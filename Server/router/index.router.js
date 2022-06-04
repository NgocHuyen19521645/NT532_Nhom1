const Router=require('express').Router();

const iotRouter=require('./iot/iot');

Router.use('/iot',iotRouter);


module.exports=Router;
