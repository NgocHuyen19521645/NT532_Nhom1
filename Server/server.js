const express=require('express');
const app=express();
const bodyParser=require('body-parser');
const mongoose=require('mongoose');
const cors=require('cors');
const indexRouter=require('./router/index.router');
app.use(bodyParser.json());
app.use(cors());
app.use(bodyParser.urlencoded({extended:true}));

// const mongoAtlasUri = 'mongodb+srv://iot:123@cluster0.rziap.mongodb.net/myFirstDatabase?retryWrites=true&w=majority';
// mongoose.connect(
//   mongoAtlasUri,
//   { useNewUrlParser: true, useUnifiedTopology: true },
//   () => console.log(" Mongoose is connected")
// );
app.use('/',indexRouter);
app.listen('11111',()=>{

})
