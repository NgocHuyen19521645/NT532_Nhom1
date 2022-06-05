/*
 * Kết nối:
 *          DHT                   Arduino
 *          VCC                     5V
 *          GND                     GND
 *         Tín hiệu                  2
 *
 * Tùy chọn tên của cảm biến cho phù hợp
 * Nạp code mở Serial Monitor, chọn No line ending, baud 9600
 */
#include "DHT.h"
#include "ESP8266WiFi.h"
#include <ESP8266Ping.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <FirebaseArduino.h>
#include <b64.h>
#include <HTTPClient.h>
#include <ESP8266HTTPClient.h>
#include <UrlEncode.h>
#include <b64.h>
#include <ESP8266WiFi.h> // Import ESP8266 WiFi library
#include <Wire.h>
#include <WiFiClient.h>
#include <ArduinoJson.h>
#include <BH1750.h>
#include <UrlEncode.h>

#define DHTPIN 2 // what digital pin we're connected to
#define LIGHT 3
#define FIREBASE_HOST "trusty-gradient-326214-default-rtdb.asia-southeast1.firebasedatabase.app"
#define FIREBASE_AUTH "j8bRnAxzJSi5yOm0hytksLqDQPf0ktswSRYO2gqJ"

String serverName = "http://10.0.23.250:11112/iot/create";
unsigned long lastTime = 0;
unsigned long timerDelay = 5000;

// Chọn loại cảm biến cho phù hợp ---------------------------------------------------------------------------------------
#define DHTTYPE DHT11 // DHT 11
//#define DHTTYPE DHT22   // DHT 22  (AM2302), AM2321
//#define DHTTYPE DHT21   // DHT 21 (AM2301)

DHT dht(DHTPIN, DHTTYPE);

// Wifi SSID, pass
const char *ssid = "Giangnam Coffee";
const char *pass = "";

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org");

void setup()
{
  Serial.begin(9600);
  pinMode(D2,OUTPUT);
  Serial.println("DHTxx test!");

  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.hostname("Wemos D1");
  WiFi.begin(ssid);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");

  // Print the IP address
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);

  dht.begin();
  timeClient.begin();
  timeClient.setTimeOffset(7 * 3600);
}

int i = 0;
long sample_time = 10000; // ms

String getCurrentTime(unsigned long &epoch)
{
  unsigned long epochTime = timeClient.getEpochTime();
  struct tm *ptm = gmtime((time_t *)&epochTime);
  int dd = ptm->tm_mday;
  int mm = ptm->tm_mon + 1;
  int yy = ptm->tm_year + 1900;
  String currentTime = timeClient.getFormattedTime() + " " + String(dd) + "/" + String(mm) + "/" + String(yy);
  epoch = epochTime;
  return currentTime;
}

void loop()
{
  // Wait a few seconds between measurements.
  delay(sample_time);

  timeClient.update();

  float gas = analogRead(A0);
  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  float h = dht.readHumidity();
  // Read temperature as Celsius (the default)
  float t = dht.readTemperature();
  // Read temperature as Fahrenheit (isFahrenheit = true)
  float f = dht.readTemperature(true);
  unsigned long epoch;
  String curTime = getCurrentTime(epoch);
  // Kiểm tra có đọc được dữ liệu từ sensor hay không
  if (isnan(h) || isnan(t) || isnan(f))
  {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }
  Serial.println(curTime);
  // Compute heat index in Fahrenheit (the default)
  float hif = dht.computeHeatIndex(f, h);
  // Compute heat index in Celsius (isFahreheit = false)
  float hic = dht.computeHeatIndex(t, h, false);

  Serial.print("Gas: ");
  Serial.println(gas);
  Serial.print("Humidity: ");
  Serial.print(h);
  Serial.print(" %t");
  Serial.print("Temperature: ");
  Serial.print(t);
  Serial.print(" *C ");
  Serial.print(f);
  Serial.print(" *Ft");
  Serial.print("Heat index: ");
  Serial.print(hic);
  Serial.print(" *C ");
  Serial.print(hif);
  Serial.println(" *F");

  DynamicJsonBuffer jsonBuffer;
  // Push to Firebase
  JsonObject &temperatureObject = jsonBuffer.createObject();
  temperatureObject["Temperature"] = t;
  temperatureObject["Humidity"] = h;
  temperatureObject["Gas"] = gas;
  temperatureObject["Time"] = curTime;
  Firebase.set("/DHTSensor/" + String(epoch), temperatureObject);
  int a = Firebase.getFloat("/controlSignal/fireFlag");
  Serial.println(a);
  if(a == 0){
    digitalWrite(D2, LOW);
  }
  else{
    digitalWrite(D2, HIGH);
  }
    

  if (Firebase.failed())
  {
    Serial.println("Setting failed");
    Serial.println(Firebase.error());
    return;
  }
  WiFiClient client;
  /*
  int HTTP_PORT = 11112;
  String HTTP_METHOD = "POST";       // 172.31.250.6172.31.0.1
  char HOST_NAME[] = "10.0.23.250"; // dia chi ip (luu y thay doi)
  String PATH_NAME = "/iot/create";
  String data = "temp=" + String(t) + "&gas=" + String(gas);
  Serial.println(data);
  // Serial.println(client.connect(HOST_NAME, HTTP_PORT));
  int data_length = data.length();

  if (client.connect(HOST_NAME, HTTP_PORT))
  {
    Serial.println("Connected to server");

    client.println("POST " + PATH_NAME + " HTTP/1.1");
    client.println("Host: " + String(HOST_NAME));
    client.println("Content-Type: application/x-www-form-urlencoded");
    client.println("Content-Length: " + String(data_length));
    client.println("Connection: close");
    client.println();
    client.println(data);
    client.stop();
  }
  String path = "/iot/data";
  if (client.connect(HOST_NAME, HTTP_PORT))
  {
    Serial.println("Connected to server 2");
    client.println("GET " + path + " HTTP/1.1");
    client.println("Host: " + String(HOST_NAME));
    client.println("Connection: close");
    client.println();
    String c = "aAa";
    while (client.connected())
    {
      if (client.available())
      {
        while (client.available())
        {
          c = client.readStringUntil('\n');
          if (c[0] == '{')
          {
            Serial.println(c[c.length() - 3]);
            // deserializeJson(doc, c);
            break;
          }
        }
        Serial.println("----------------------");
        Serial.println((String)c[c.length() - 3]);
        Serial.println("----------------------");
        if ((String)c[c.length() - 3] == "1")
        {
          digitalWrite(D2, HIGH);
        }
        else
          digitalWrite(D2, LOW);
      }
      // the server's disconnected, stop the client:
      
    }
    client.stop();
  }
  delay(5000);*/
}