Power Lunch Mobile App
====================

Background
-------------------

A typical household consumes more electricity for heating and cooling purposes than any other single use and the refrigerator is often the most power hungry appliance of all. According to the second law of thermodynamics, for all of the heat removed from the interior of the fridge to keep it's contents cool, a greater amount is added outside where it's released and distributed throughout the room. It's not difficult to imagine a better use for this heat in a household that features such appliances as ovens, cloths dryers, and water heaters. The challenges come from transferring heat between locations and customers not yet ready for large, expensive, multipurpose appliances.
Our team decided to address this challenge on a smaller scale placing a thermoelectric cooling plate inside of a lunch box or cooler to simultaneously heat and cool the contents. Further, since a user generally doesn't care about the temperature of the food until they are about to consume it, the timing of the heating and cooling can be controlled and adjusted to so that it is only active when needed.

Description
-------------

The Power Lunch lunchbox is a network connected insulated food container, such as a lunchbox or cooler, with the interior divided into two sections by a thermoelectric cooling module such that when powered, one side of the container is heated while the other is cooled. The container connects to a mobile app through bluetooth allowing the user to manage the container's power consumption and timing of the hearter/cooler so that it's contents reach the ideal temperatures immediately before consumption.

How it works
----------------

A control board connects to a relay directing current to an array of theromoelectric coolers. The coolers, with heat sinks attached on one side to prevent the hot side from becoming too hot, are sandwiched between conductive plates that make up the hot and cold surfaces of the lunchbox. The control board features a bluetooth module making the relays, and therefore the temperature of the plates, controllable from a mobile device application. A set of temperature sensors connected to the control board return the temperature of each plate back to the mobile application.
The entire module is them placed inside an insulated cooler or lunchbox such that it forms two separate sealed compartments. The module com be powered by a rechargeable battery and if needed, feature a fan on each side for circulating air within each compartment.

How we built it
--------------------

The thermoelectric heating and cooling module is assembled using by first placing a conductive copper sheet into a 3D printed holder. Next, two thermoelectric plates coated with thermal paste and with leads connected in series are pressed flat against the plate. The exposed surfaces of the plates are also coated with thermal paste and and heat sinks are the pressed flat against them. Finally, a second copper sheet is pressed against the heat sinks and held in place by the printed holder.
The control circuit consists of an Intel powered Arduino 101 control board with a base shield connected to two temperature sensors and a relay. The output terminals on the relay are connected between the thermoelectric module and it's power source allowing the control board to activate or deactivate the module. The control circuit components are housed in a 3D printed container placed next to the thermoelectric module and the temperature sensors are attached to each of the copper sheets.
The Arduino 101 board is programmed to run a bluetooth low energy service, transmit two temperature characteristics, and listen for a digital output characteristic. The service and characteristic IDs are used in the Power Lunch mobile app to identify and communicate with the lunchbox.
The Power Lunch Android app uses the Android Bluetooth Low Energy API to query all nearby bluetooth devices by name and connect if the name matches that of the lunchbox. Once a connection is established, the app queries available services and the characteristics by ID to find those the match the temperature readings of the two sensors. The temperatures are read and lunchbox preview is updated so the color intensity of each compartment reflects the measured temperature. The lunchbox can be activated or deactivated by querying and then writing to the control boards activation service.

Advantages
-------------------------

The termoelectric coolers provides a quick, compact, and inexpensive way for cooling and heating, however it they are not very energy efficient. A control system minimizes the energy use to create a product that is practical and suited for everyday use. While the technology is easiest to implement in a lunchbox, it could also have applications within containers for food delivery or within larger home and kitchen appliances. 
