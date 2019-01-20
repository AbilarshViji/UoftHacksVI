CLIENT_ID = '99895afc-444b-4f66-ab77-d86e21e4909e'
CLIENT_SECRET = '11d38821-f9d8-4945-974f-f7fdd9aab9da'
REDIRECT_URI = 'https://eba09900.ngrok.io/exchange'
import smartcar
from flask import Flask, redirect, request, jsonify
from flask_cors import CORS

import os

app = Flask(__name__)
CORS(app)

# global variable to save our access_token
access = None

client = smartcar.AuthClient(
    client_id=CLIENT_ID,
    client_secret=CLIENT_SECRET,
    redirect_uri=REDIRECT_URI,
    scope=['read_vehicle_info', 'read_location', 'control_security', 'control_security:unlock', 'control_security:lock', 'read_odometer'],
    test_mode=False
)


@app.route('/login', methods=['GET'])
def login():
    auth_url = client.get_auth_url()
    print(auth_url)
    return redirect(auth_url)


@app.route('/exchange', methods=['GET'])
def exchange():
    code = request.args.get('code')

    # access our global variable and store our access tokens
    global access
    # in a production app you'll want to store this in some kind of
    # persistent storage
    access = client.exchange_code(code)
    return '', 200


@app.route('/vehicle', methods=['GET'])
def vehicle():
    # access our global variable to retrieve our access tokens
    global access
    # the list of vehicle ids
    vehicle_ids = smartcar.get_vehicle_ids(
        access['access_token'])['vehicles']

    # instantiate the first vehicle in the vehicle id list
    vehicle = smartcar.Vehicle(vehicle_ids[0], access['access_token'])
    location = vehicle.location()
    info = vehicle.info()
    print(info)
    print(access['access_token'])
    vehicle.unlock()
    return jsonify(vehicle.unlock())


if __name__ == '__main__':
    app.run(port=8000)
