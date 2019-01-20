import smartcar
access_token = '60afaa60-f78c-4dc0-ab2c-f3923d44b350'
request = smartcar.get_vehicle_ids(access_token)
vid = request['vehicles'][0]
vehicle = smartcar.Vehicle(vid, access_token)
location = vehicle.location()
print(location)
odo = vehicle.odometer()
print(odo)
