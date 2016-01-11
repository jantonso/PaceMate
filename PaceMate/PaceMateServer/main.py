import webapp2

import logging

from google.appengine.api import users
from google.appengine.ext import ndb

import json

def workoutrecord_key(user_name):
    """Constructs a Datastore key for a WorkoutSplit entity.

    We use user_name as the key.
    """
    return ndb.Key('Workouts', user_name or "nouser")

class WorkoutSplit(ndb.Model):
	user_name = ndb.StringProperty()
	split_time = ndb.IntegerProperty()
	split_dist = ndb.StringProperty()	
	workout_num = ndb.IntegerProperty()

class MainPage(webapp2.RequestHandler):
	def get(self):
		user_name = self.request.get('user_name')

		logging.info("fetching data")
		workout_query = WorkoutSplit.query(ancestor=workoutrecord_key(user_name)).order(-WorkoutSplit.workout_num,WorkoutSplit.split_time)
		workoutdata = workout_query.fetch(500)

		logging.info(len(workoutdata))
		workouts = []
		split_times = []
		split_distances = []
		workout_num = -1
		if len(workoutdata) > 0:
			workout_num = workoutdata[0].workout_num
		for w in workoutdata:
			if (w.workout_num == workout_num):		
				split_times += [w.split_time]
				split_distances += [w.split_dist]
			else:
				workouts += [{'workout_num': workout_num, 'split_times': split_times, 'split_distances': split_distances}]
				workout_num = w.workout_num
				split_times = [w.split_time]
				split_distances = [w.split_dist]
		workouts += [{'workout_num': workout_num, 'split_times': split_times, 'split_distances': split_distances}]
		obj = {'user_name': user_name, 'workouts': workouts}
		json_obj = json.dumps(obj)
		
		logging.info("returning response")
		self.response.write(json_obj)
	def post(self):
		logging.info("we got a new post request!")
		user_name = self.request.get('user_name')

		new_workout = WorkoutSplit(parent=workoutrecord_key(user_name)) 
		new_workout.split_dist = self.request.get('split_dist')
		new_workout.user_name = user_name
		try:
			new_workout.split_time = int(self.request.get('split_time'))
			new_workout.workout_num = int(self.request.get('workout_num'))	
			new_workout.put()
			self.response.write('Succesfully stored split')
		except ValueError:
			logging.info("it failed...")			

application = webapp2.WSGIApplication([
    ('/.*', MainPage),
], debug=True)

