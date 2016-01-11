//lets require/import the mongodb native drivers.
var mongodb = require('mongodb');

//We need to work with "MongoClient" interface in order to connect to a mongodb server.
var MongoClient = mongodb.MongoClient;

//File system
var fs = require('fs');

// Connection URL. This is where your mongodb server is running.
var url = 'mongodb://localhost:27017/mydb';

module.exports = {
    get_workouts_for_user: function(username, callback){
        MongoClient.connect(url, function (err, db) {
            if (err) {
              console.log('Unable to connect to the mongoDB server. Error:', err);
            } else {
              //HURRAY!! We are connected. :)
              console.log('Connection established to', url);

              collection = db.collection('Workout');

			  // Exclude returning the actual file data to save bandwidth
              collection.find({ $or: [{coach: username},{athlete: username}]}).toArray(function(err, results) {
                if (err) {
                  console.log(err);
                } else {
                  // Callback is the function to run w/ the results
		   		   console.log(results);
                   callback(results);
				}
                db.close();
              }); 
            }
          });
    },

    add_workout: function(date, coach, athlete, description, target_pace, target_distance, split_distances, split_times, callback){
        // Use connect method to connect to the Server
        MongoClient.connect(url, function (err, db) {
              if (err) {
                console.log('Unable to connect to the mongoDB server. Error:', err);
              } else {
                //HURRAY!! We are connected. :)
                console.log('Connection established to', url);

                collection = db.collection('Workout');

				var new_workout = {date: date, coach: coach, athlete: athlete, description: description, target_pace: target_pace, target_distance: target_distance, split_distances: split_distances, split_times: split_times};
				collection.insert(new_workout, function(err, result) {
				  if (err) {
					console.log(err);
					console.log("Error");
				  } else {
					console.log('Inserted: ', result);
				  }
                  callback(err, new_workout._id);
                  db.close();
				});
              }
            });
    },

	update_workout: function(workout_id, split_distances, split_times, callback){
        // Use connect method to connect to the Server
        MongoClient.connect(url, function (err, db) {
              if (err) {
                console.log('Unable to connect to the mongoDB server. Error:', err);
              } else {
                //HURRAY!! We are connected. :)
                console.log('Connection established to', url);

                collection = db.collection('Workout');
				
				var id = new mongodb.ObjectID(workout_id);              

                collection.update(
                    {"_id": id},
                    {
                      $set: {"split_distances": split_distances, "split_times": split_times}
                    }, function(err, result) {
						if (err) {
                    		console.log(err);
                  		} else {
                    		console.log('Updated: ', result);
                 	 	}
                  		callback(err);
                  		db.close();
					});
              }
            });
    }
}
