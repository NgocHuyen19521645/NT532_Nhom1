const express=require('express');
const app=express();
const bodyParser=require('body-parser');
const mongoose=require('mongoose');
const cors=require('cors');
const indexRouter=require('./router/index.router');
app.use(bodyParser.json());
app.use(cors());
app.use(bodyParser.urlencoded({extended:true}));
var fireFlag=0;
var temp=0;
var gas=0;

// const mongoAtlasUri = 'mongodb+srv://iot:123@cluster0.rziap.mongodb.net/myFirstDatabase?retryWrites=true&w=majority';
// mongoose.connect(
//   mongoAtlasUri,
//   { useNewUrlParser: true, useUnifiedTopology: true },
//   () => console.log(" Mongoose is connected")
// );
app.use('/',indexRouter);
app.listen('11112',()=>{

})

var admin = require("firebase-admin");
var serviceAccount = require("./admin.json");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://trusty-gradient-326214-default-rtdb.asia-southeast1.firebasedatabase.app"
});
// var db=admin.database();
// var userRef=db.ref("users");
// userRef.update({
//     alanisawesome: {
//       date_of_birth: 'June 23, 1912',
//       full_name: 'GB'
//     },
//     gracehop: {
//       date_of_birth: 'December 9, 1906',
//       full_name: 'TG'
//     }
//   });

/////
const db = admin.database();
const ref = db.ref('DHTSensor').limitToLast(1);

// Attach an asynchronous callback to read the data at our posts reference
ref.on('value', (snapshot) => {
    var key = Object.keys(snapshot.val());
    temp = snapshot.val()[key].Temperature;
    gas = snapshot.val()[key].Gas;
    console.log(temp, gas);

    if(temp>0 & gas>0) {
        fireFlag=1;
    }
    else {
        fireFlag=0;
    }

    var userRef=db.ref("/controlSignal");
    userRef.update({
        fireFlag: fireFlag
    });

}, (errorObject) => {
  console.log('The read failed: ' + errorObject.name);
});

  

