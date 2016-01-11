#include <Bridge.h>
#include <YunServer.h>
#include <YunClient.h>
#include <HttpClient.h>

int theLEDs[] = {2, 3, 4, 5, 6, 7, 8, 9};
int curLED = 0;
int clock = 0;
int curState = 0;
int baudRate = 9600;
int curPace;
int IRDetected;
int numDelays;
int newNumDelays;
int enteredTime;
int splitNum = 1;
bool started = false;

YunServer server;
YunClient client;

/* Helpers */

void setupLEDPins()
{
	for(int i = 0; i < 8; i++){
		pinMode(theLEDs[i], OUTPUT);
                //digitalWrite(theLEDs[i], LOW);
	}
}

void setPace(int totalTime)
{
	enteredTime = totalTime;
	float pace = totalTime / 8; // 8 points in system
	float newNumDelays = pace / .2;
	numDelays = newNumDelays;
}

void changeLED()
{
	digitalWrite(curLED, LOW);

	if(curLED == 9){
		curLED = 2;
		setPace(16); //to fix time set by IR sensor
		IRDetected = 0;
	}else{
		curLED++;
	}
}

bool readIR()
{
	int reading = analogRead(0);
	if(reading > 400){
		return true;
	}
	return false;
}

void runLEDs()
{
	digitalWrite(curLED, HIGH);

	if(readIR()){
		//need to send current time to server(clock * 200 milliseconds)
		float timePassedSinceStart = ((curLED-1) * numDelays * .2) + clock * .2;
		if(timePassedSinceStart < enteredTime && IRDetected == 0){
			
                        recordsplit("jantonso", timePassedSinceStart, String(splitNum), 1);
                        splitNum += 1;
  
                        IRDetected = 1;
			float timeLeft = enteredTime - timePassedSinceStart;
			float remainingPace = timeLeft / 3; // 3 because only 3 LEDs after duino
			newNumDelays = remainingPace / .2;
			digitalWrite(curLED, LOW);
			curLED = 6;
			clock = 0;
			Serial.print("It took this much time to walk to IR sensor: ");
			Serial.println(timePassedSinceStart);
		}
	}
	

	if(clock == numDelays && IRDetected == 0){
		changeLED();
		clock = 0;
	}else if(clock == newNumDelays && IRDetected == 1){
		changeLED();
		clock = 0;
	}
	
	delay(200);
	clock++;
}


/* ARDUINO REQUIRED FUNCTIONS */

void setup()
{
   	Serial.begin(baudRate);
	clock = 0;
	IRDetected = 0;
	setPace(16);
	setupLEDPins();
        digitalWrite(13, HIGH);
        Bridge.begin();
        
        server.noListenOnLocalhost();
        server.begin(); 
        digitalWrite(13, LOW);       
}

void loop()
{
	switch(curState){
		case 0:
			client = server.accept();

                        if (client) {
                          process(client);
                          client.stop();
                        }		
			break;
		case 1:
			//waitForStart();
			break;
		case 2:
			runLEDs();

                        // Check if they tried to stop
                        client = server.accept();
                        if (client) {
                            String command = client.readStringUntil('/');
                            if (command == "stop") {
                               client.print("stopping");
                               curState = 0;
                               setPace(16);
                               digitalWrite(curLED, LOW);
                             }
                        }                     
			break;
	}
}

void process(YunClient client) {
  String command = client.readStringUntil('/');

  if (command == "pace") {
    int pace_val = client.parseInt();
    client.print(pace_val); 
    
    setPace(pace_val); 
  }
  if (command == "start") {
    if (curState == 0) {
      client.print("starting");
      //workout_num += 1;
      curLED = 2;
      curState = 2;
    }
  } 
}

void recordsplit(String user_name, int split_time, String split_dist, int workout_num) {
  Process p;
  p.runShellCommand("curl --data \"user_name=" + user_name + "&split_time=" + split_time 
    + "&split_dist=" + split_dist + "&workout_num=" + workout_num + "\" + http://pelagic-plexus-87318.appspot.com/"); 
  Console.println("sent command");
  p.close();
}
