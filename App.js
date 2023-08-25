import React, { useState, useEffect } from "react";
import { NativeEventEmitter, NativeModules, Button, Text, View } from "react-native";

const App = () => {
  const [location, setLocation] = useState(null);
  console.log("Location module");
  const { LocationModule } = NativeModules;
  console.log('LocationModule: ', LocationModule);
  const locationEventEmitter = new NativeEventEmitter(LocationModule);
  const [click , setClick] = useState(0)

  useEffect(() => {
    const locationUpdateListener = locationEventEmitter.addListener(
      "onLocationUpdate",
      (newLocation) => {
        setLocation(newLocation);
        console.log("Received location update:", newLocation);
      }
    );

    return () => {
      locationUpdateListener.remove();
    };
  }, [click]);

  const getCurrentLocation = () => {

    LocationModule.startLocationUpdates()
  }

  const getFetchLocation =()=>{
    setClick(preV => preV+1)
    LocationModule.fetchLocation()
  }

  return (
    <View>
      <Button title="Start Location Updates" onPress={() => getCurrentLocation() } />

      <Button title="Fetch Updates" onPress={() => getFetchLocation() } />
      {location && (
        <View>
          <Text>Latitude: {location.latitude}</Text>
          <Text>Longitude: {location.longitude}</Text>
          <Text>accuracy: {location.accuracy}</Text>

        </View>
      )}
    </View>
  );
};

export default App;
