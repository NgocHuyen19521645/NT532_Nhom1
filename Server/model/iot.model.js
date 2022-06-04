const mongoose=require('mongoose');
const iotSchema=new mongoose.Schema({
    temp:{
        type : Number,
        required : true
    },
    gas:[{
        type: Number,
        required : true
    }],
})
module.exports=mongoose.model('iot',iotSchema);
