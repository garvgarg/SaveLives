
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

Parse.Cloud.define("needhelp", function(request, response) {

  //Find users near a given location	  
  var query = new Parse.Query("User");
  var lat = (request.params.lat).toString();
  var lng = (request.params.lng).toString();
  var approxaddress = request.params.address;
  var street = request.params.street;
  var city = request.params.city;
  var state = request.params.state;
  var zip = request.params.zip;
  var country = request.params.country;
  var userid = request.params.userid;
  //var user = Parse.User.current();
  var phone = request.params.phone;
  //query.equalto("objectid",userid);
  console.error(request.params);
  //query.find
  var point = new Parse.GeoPoint(request.params.lat, request.params.lng);
  query.withinMiles("location" , point, 10);
  
  // Find devices associated with these users
  var pushQuery = new Parse.Query(Parse.Installation);
  pushQuery.matchesQuery('user', query);
  
  var volunteerQuery = new Parse.Query("User");
  volunteerQuery.matchesQuery("volunteerNuser", pushQuery);
  // Send push notification to query
  Parse.Push.send({
    where: pushQuery,
    data: {
      alert: "Need Help!",
      location: approxaddress,
      street: street,
      city: city,
      state: state,
      zip: zip,
      country: country,
      phone: phone,
      latitude: lat,
      longitude: lng
    }
  }, {
    success: function() {
    	console.error("succeeded");
    	response.success("Help is coming!");
      // Push was successful
    },
    error: function(error) {
    	console.error("Failed");
    	response.success("Try again!");
      // Handle error
    }
  });
  
 /* 
  query.find({
    success: function(results) {
      var str = "These are nearby people ";
      for (var i = 0; i < results.length; ++i) {
    	  str += results[i].get("name");
    	  str += (" ");
      }
      console.error("Query was success");
      response.success(str);
    },
    error: function() {
      response.error("No one nearby");
    }
  }); */
});