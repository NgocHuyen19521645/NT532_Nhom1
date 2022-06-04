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

#define DHTPIN 2     // what digital pin we're connected to
#define LIGHT 3
#define FIREBASE_HOST "trusty-gradient-326214-default-rtdb.asia-southeast1.firebasedatabase.app"
#define FIREBASE_AUTH "j8bRnAxzJSi5yOm0hytksLqDQPf0ktswSRYO2gqJ"

// Chọn loại cảm biến cho phù hợp ---------------------------------------------------------------------------------------
#define DHTTYPE DHT11   // DHT 11
//#define DHTTYPE DHT22   // DHT 22  (AM2302), AM2321
//#define DHTTYPE DHT21   // DHT 21 (AM2301)

DHT dht(DHTPIN, DHTTYPE);

// Wifi SSID, pass
const char* ssid = "UiTiOt-E3.1";
const char* pass = "UiTiOtAP";

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org");

void setup() {
  Serial.begin(9600);
  Serial.println("DHTxx test!");
  
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.hostname("Wemos D1");
  WiFi.begin(ssid, pass);
 
  while (WiFi.status() != WL_CONNECTED) {
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
  timeClient.setTimeOffset(7*3600);
}

int i = 0;
long sample_time = 10000; //ms

String getCurrentTime(unsigned long &epoch)
{
  unsigned long epochTime = timeClient.getEpochTime();
  struct tm *ptm = gmtime ((time_t *)&epochTime);
  int dd = ptm->tm_mday;
  int mm = ptm->tm_mon+1;
  int yy = ptm->tm_year+1900;
  String currentTime = timeClient.getFormattedTime()
                      + " " + String(dd)
                      + "/" + String(mm)
                      + "/" + String(yy);
  epoch = epochTime;
  return currentTime;
}

void loop() {
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
  if (isnan(h) || isnan(t) || isnan(f)) {
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
  JsonObject& temperatureObject = jsonBuffer.createObject();
  temperatureObject["Temperature"] = t;
  temperatureObject["Humidity"] = h;
  temperatureObject["Gas"] = gas;
  temperatureObject["Time"] = curTime;
  Firebase.set("/DHTSensor/"+String(epoch), temperatureObject);
  
 if(Firebase.failed())
  {
    Serial.println("Setting failed");
    Serial.println(Firebase.error());
    return;
  }
}
