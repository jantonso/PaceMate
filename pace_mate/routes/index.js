var express = require('express');
var passport = require('passport');
var Account = require('../models/account');
var router = express.Router();
var db = require('../db_util.js');
var plotly = require('plotly')('aayush21', 'haco5dh4io')

router.get('/', function (req, res) {
	res.render('index', { user : req.user });
});

router.get('/register', function(req, res) {
	res.render('register', { });
});

router.post('/register', function(req, res) {
	Account.register(new Account({ username : req.body.username }), req.body.password, function(err, account) {
		if (err) {
			return res.render('register', { account : account });
		}

		passport.authenticate('local')(req, res, function () {
			res.redirect('/account_home');
		});
	});
});

router.get('/login', function(req, res) {
	res.render('login', { user : req.user });
});

router.post('/login', passport.authenticate('local'), function(req, res) {
	res.redirect('/account_home');
});

router.get('/logout', function(req, res) {
	req.logout();
	res.redirect('/');
});

router.get('/ping', function(req, res){
	res.status(200).send("pong!");
});

router.get('/account_home', function(req, res){
	var un = req.user.username
	console.log(req.user);
	db.get_workouts_for_user(un, function(data1) {
		var data = [
		{
			x: data1[0].split_distances,
			y: data1[0].split_times,
			type: "scatter"
		}
		];
		var graphOptions = {filename: "run_info", fileopt: "overwrite"};
		plotly.plot(data, graphOptions, function (err, msg) {
			var out_data = {
				user: req.user,
				workouts: data1,
				url_to_plot: msg.url
			}
			res.render('account_home', out_data);
		});
	});
});

router.get('/add_workout', function(req, res){
	res.render('add_workout',{user: req.user});
});

router.post('/add_workout', function(req, res){
	var coach = req.body.coach;
	var athlete = req.body.athlete;
	var description = req.body.description;
	var date = req.body.date;
	var target_pace = req.body.target_pace;
	var target_dist = req.body.target_distance;
	var split_distances = req.body.split_distances.split(",");
	var split_times = req.body.split_times.split(",");
	db.add_workout(date,coach,athlete,description,target_pace,target_dist,split_distances,split_times, function(error, workout_id) {
		if (error) {
			console.log("insert operation failed... " + error);
			res.render('add_workout',{user: req.user, error_message: "Insert operation failed..."});
		} else {
			res.redirect('/account_home');
		}
	});
});

// For android app to create a new workout
router.post('/create_new_workout', function(req, res){
	var coach = req.body.coach;
	var athlete = req.body.athlete;
	var description = req.body.description;
	var date = req.body.date;
	var target_pace = req.body.target_pace;
	var target_dist = req.body.target_distance;
	db.add_workout(date,coach,athlete,description,target_pace,target_dist,"","", function(error, workout_id) {
		if (error) {
			console.log("insert operation failed... " + error);
			res.status(500).send("Couldn't insert..");
		} else {
			// Return workout_id
			res.status(200).send(workout_id);
		}
	});
});

// For arduino to update workout splits
router.post('/update_workout', function(req, res) {
	var id = req.body.workout_id;
	var split_distances = req.body.split_distances.split(",");
	var split_times = req.body.split_times.split(",");
	//var id = "5657a99345d2afd5791eb7d2";
	//var split_distances = ["1","2","3","4"];
	//var split_times = ["a", "b", "c", "d"];

	db.update_workout(id, split_distances, split_times, function(error) {
		if (error) {
			console.log("update operation failed... " + error);
			res.status(500).send("Couldnt update...");
		} else {
			res.status(200).send("Updated succesfully!");
		}
	});
});

module.exports = router;
