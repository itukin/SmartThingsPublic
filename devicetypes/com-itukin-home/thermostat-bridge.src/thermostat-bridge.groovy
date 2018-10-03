/**
 *  Thermostat Bridge
 *
 *  Copyright 2018 Ivan Tukin
 *
 */
metadata {
	definition (name: "Thermostat Bridge", namespace: "com.itukin.home", author: "Ivan Tukin") {
		
		capability "Polling"
		capability "Refresh"
		capability "Thermostat"
        capability "Temperature Measurement"
        capability "Sensor"
        capability "Switch"
	}


	preferences {
		
		
 	}

	tiles {
		valueTile("temperature", "displayTemperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', unit:"C",
				backgroundColors:[
        		[value: 0, color: "#153591"],
				[value: 7, color: "#1e9cbb"],
				[value: 15, color: "#90d2a7"],
				[value: 23, color: "#44b621"],
				[value: 29, color: "#f1d801"],
				[value: 35, color: "#d04e00"],
				[value: 36, color: "#bc2323"]
        ]
			)
		}
		
        standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "off",  action:"on", icon:"st.thermostat.heating-cooling-off"		
			state "heat",  action:"off", icon:"st.thermostat.heat"			
		}	

		
        valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}° heat', unit:"C", backgroundColor:"#ffffff"
		}
		
        standardTile("refresh", "device.temperature", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		


		
		

		main "temperature"
		details([ "temperature", 
        "mode", 
        "hvacStatus",
        
        "heatingSetpoint",
        "refresh"
        ])
	}
   command "on"
   command "off"
}

preferences {
 input("deviceId", "text", title: "Device ID", required: true, displayDuringSetup: true)
 input("gatewayIP", "text", title: "Gateway IP", required: true, displayDuringSetup: true)
 input("gatewayPort", "text", title: "Gateway Port", required: true, displayDuringSetup: true)
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"	

}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	log.trace "poll called"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    executeCommand("status")
	// TODO: handle 'refresh' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
    executeCommand("heat");

}

def off() {
	log.debug "Executing 'off'"
    sendEvent(name: "thermostatMode", value: "off" , isStateChange: true)
	executeCommand("off")

	// TODO: handle 'off' command
}

def setHeatingSetpoint( Double d) {
	log.debug "Executing 'setHeatingSetpoint'"
	// TODO: handle 'setHeatingSetpoint' command
}

def heat() {
	log.debug "Executing 'heat'"
	// TODO: handle 'heat' command
    sendEvent(name: "thermostatMode", value: "heat", isStateChange: true)
	executeCommand("heat");

}

private executeCommand(command){

   def gatewayIPHex = convertIPtoHex(gatewayIP)
   def gatewayPortHex = convertPortToHex(gatewayPort)

   def headers = [:] 
   headers.put("HOST", "$gatewayIP:$gatewayPort")    

   def path =  "/climate/$command?device=$deviceId"

   try {
     sendHubCommand(new physicalgraph.device.HubAction([
         method: "GET",
         path: path,
         headers: headers], 
         device.deviceNetworkId, 
         [callback: "hubActionResponse"]
     )) 
   } catch (e) {
     message(e.message)
   }
}

def hubActionResponse(response){


 def data = response.json ?: ""
 message("response: ${data}")
 if (data) {
     def curr_temp = data?.attributes?.current_temperature ?: ""
     def target_temp = data?.attributes?.temperature ?: ""
     def status = data?.state ?: ""
     if (curr_temp ){
        message("setting temperature to display: ${curr_temp}")
        sendEvent(name: "displayTemperature", value: curr_temp , isStateChange: true)
     }

     if (target_temp) {
        message("setting target temperature to display: ${target_temp}")
        sendEvent(name: "heatingSetpoint", value: target_temp , isStateChange: true)
     }
     
     if (status){
     	message("setting state to display: ${status}")
        
        sendEvent(name: "thermostatMode", value: status=="Heat (Default)"? "heat": "off" , isStateChange: true)
     }
 }
/* def status = data?.relay_state;

 if (status != 0 && status != 1){
 	status = ""
 }
 def cons = data?.emeter?.get_realtime?.total ?: ""

 message("switch consumption: '${cons}'")
 message("switch status: '${status}'")

 if (status != "") {
     sendEvent(name: "switch", value: status == 1 ? 'on' : 'off', isStateChange: true)
 }

 if (cons != "") {
 	sendEvent(name: "energy", value: cons, isStateChange: true)
 }*/

}

private String convertIPtoHex(ipAddress) {
   String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
   return hex
}

private String convertPortToHex(port) {
   String hexport = port.toString().format( '%04x', port.toInteger() )
   return hexport
}

def message(msg){
 log.debug(msg)
}