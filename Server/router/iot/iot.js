const Router=require('express').Router();
const res = require('express/lib/response');
const iotController=require('../../controller/iot');

Router.post('/create', create);
Router.get('/data', get);


function create(req, res) {
  const {temp, gas} = req.body;
  iotController.create(temp, gas)
  .then( data => {
    res.json(data);
  })
  .catch(err => {
    res.json(err);
  })
}

function get(req, res) {
  iotController.get()
  .then( data => {
    res.json(data);
  })
  .catch(err => {
    res.json(err);
  })
}

module.exports=Router;
