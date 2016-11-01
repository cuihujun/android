# Zephyr Health Monitor

The Zephyr Health Monitor is a fork from the nRF Toolbox which can be found at https://github.com/NordicSemiconductor/Android-nRF-Toolbox

It contains applications demonstrating Bluetooth Smart profiles: 
* **Heart Rate Monitor**, 
* **Health Thermometer Monitor**, 
* **Pulse Oximeter Monitor**,

The corresponding hardware that works with this application is an Arduino 101 using the Zephyr RTOS.  This project can be found at https://github.com/zephyrhealthproject/zephyr.

The graph in HRM profile is created using the [AChartEngine v1.1.0](http://www.achartengine.org) contributed based on the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).

### Note
- Android 4.3 or newer is required.
- Tested on Nexus 4, Nexus 7, Samsung S3 and S4 with Android 4.3 and on Nexus 4, Nexus 5, Nexus 7, Nexus 9 with Android 4.4.4 and 5.

### Known problems
- Nexus 4 and Nexus 7 with Android 4.3 do not allow to unbind devices.
- Reconnection to bondable devices may not work on several tested phones.
- Nexus 4, 5 and 7 with Android 4.4 fails if reconnecting when Gatt Server is running.
- Reset of Bluetooth adapter may be required if other errors appear.

