#include <Bridge.h>
#include <YunServer.h>
#include <YunClient.h>
#include <HttpClient.h>
#include <FastLED.h>
#include <SimpleTimer.h>

#define NUM_LEDS 38
#define DATA_PIN 6

int curState = 1;
int baudRate = 9600;

unsigned long currentMillis;

CRGB leds[NUM_LEDS];
int curLED = 0;
YunServer server;
YunClient client;
SimpleTimer timer;
int curTimerID;

String workoutID;
int pace_loop;
int curDistance = 0;
int targetDistance;

int cur_split_distances[100];
int cur_split = 0;
float cur_split_times[100];

void setupLEDS(){
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, NUM_LEDS);
  for(int i = 0; i < NUM_LEDS; i++){
    leds[i] = CRGB::Red;
  }
  FastLED.show();
}

void clearMemory(){
  curState = 1;
  pace_loop = 0;
  cur_split = 0;
  curDistance = 0;
  timer.disable(curTimerID);
}

void changeLED(){
  leds[curLED] = CRGB::Red;
  curLED += 1;
  if(curLED == NUM_LEDS){
    curLED = 0;
  }
  leds[curLED] = CRGB::Green;
  FastLED.show();
}

void process(YunClient client) {
  String command = client.readStringUntil('/');

  if (command.equals("pace")) {
    String pace = client.readStringUntil('/');
    client.readStringUntil('/');
    String target_distance = client.readStringUntil('/');
    client.readStringUntil('/');
    String workout_id = client.readStringUntil('/');
    workoutID = workout_id;
    workoutID.trim();
    Serial.println(pace);
    Serial.println(target_distance);
    targetDistance = target_distance.toInt();
    Serial.println(workout_id);
    client.print(pace);
    pace_loop = (pace.toInt()*1000)/NUM_LEDS;
    Serial.println(pace_loop);
    curTimerID = timer.setInterval(pace_loop, changeLED);
    curState = 2;
    cur_split = 0;
    currentMillis = millis();
  } 
}

void sendSplit(){
  String split_time = "";
  String split_dist = "";
  for(int i = 0; i < cur_split; i++){
    split_time += String(cur_split_times[i]);
    split_dist += String(cur_split_distances[i]);
    if(i != cur_split - 1){
      split_time += ",";
      split_dist += ",";
    }
  }
  
  Serial.println("sending to web server");
  Serial.println(workoutID);
  Serial.println(split_time);
  Serial.println(split_dist);
  Serial.println("---------------------");
  
  Process p;
  p.runShellCommand("curl --data \"workout_id=" + workoutID + "&split_times=" + split_time 
    + "&split_distances=" + split_dist + "\" http://128.237.165.120:3000/update_workout"); 
  p.close();
  
  clearMemory();
}


bool readIR()
{
  int reading = analogRead(0);
  //Serial.println(reading);
  if(reading < 50){
    return true;
  }
  return false;
}


/* ARDUINO REQUIRED FUNCTIONS */

void setup()
{
  Serial.begin(baudRate);
  setupLEDS();
  digitalWrite(13, HIGH);
  Bridge.begin();

  server.noListenOnLocalhost();
  server.begin();
  digitalWrite(13, LOW); 
}

void loop()
{
  switch(curState){
    case 1:
      client = server.accept();

      if (client) {
        process(client);
        client.stop();
      }   
      break;
  case 2:
      timer.run();
      if(readIR()){
        //read sonar sensor
        unsigned long newMillis = millis();
        //Serial.print("Time since last sonar sensor reading: ");
        //Serial.println(float(newMillis - currentMillis)/1000);
        if((float(newMillis - currentMillis)/1000) > 2){
          Serial.println("new sonar sensor detected.");
          curDistance += 50;
          cur_split_distances[cur_split] = curDistance;
          cur_split_times[cur_split] = float((newMillis - currentMillis)/1000);//get time somehow;
          currentMillis = newMillis;
          cur_split += 1;
          if(curDistance >= targetDistance){
            leds[0] = CRGB::Red;
            sendSplit();
          }

          leds[curLED] = CRGB::Red;
          curLED = 0;
          FastLED.show();
        }
      }
      break;
}
}
