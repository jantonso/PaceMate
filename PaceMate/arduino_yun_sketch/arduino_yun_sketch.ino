#include <Bridge.h>
#include <YunServer.h>
#include <YunClient.h>
#include <HttpClient.h>

YunServer server; 
int led1 = 13;
int workout_num = 0;
boolean started = false;

void setup() {
  Serial.begin(9600);
  pinMode(led1,OUTPUT);
 
  digitalWrite(13, HIGH);
  Bridge.begin();
  digitalWrite(13, LOW);
  
  server.noListenOnLocalhost();
  server.begin();
}

void loop() {
  YunClient client = server.accept();

  if (client) {
    process(client);
    client.stop();
  }

  delay(50);
}

void process(YunClient client) {
  String command = client.readStringUntil('/');

  if (command == "pace") {
    int pace_val = client.parseInt();
    client.print(pace_val);  
    
    digitalWrite(13, HIGH);
    delay(pace_val);
    digitalWrite(13, LOW);
    recordsplit("jantonso", pace_val, "200m", workout_num);
  }
  if (command == "start") {
    if (!started) {
      client.print("starting");
      workout_num += 1;
      digitalWrite(13, HIGH);
      delay(1000);
      digitalWrite(13, LOW);
      started = true;
    }
  } 
  if (command == "stop") {
    if (started) {
      client.print("stopping");
      digitalWrite(13, HIGH);
      delay(1000);
      digitalWrite(13, LOW);
      started = false;
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
