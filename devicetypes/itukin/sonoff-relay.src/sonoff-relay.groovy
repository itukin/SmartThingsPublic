metadata {
	definition (name: "Sonoff Relay", namespace: "itukin", author: "Ivan Tukin") {
		capability "Polling"

		capability "Switch"

        capability "Refresh"
  	}

	tiles {
       standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
           state "off", label: 'Off', action: "switch.on",
                 icon: "st.switches.switch.off", backgroundColor: "#ffffff"
           state "on", label: 'On', action: "switch.off",
                 icon: "st.switches.switch.on", backgroundColor: "#79b821"
       }

      
       standardTile("refresh", "device.refresh", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

       main("switch")
       details(["switch", "refresh"])
	}

   command "on"
   command "off"
}

preferences {
 input("deviceId", "text", title: "Device", required: true, displayDuringSetup: true)
 input("gatewayIP", "text", title: "Gateway IP", required: true, displayDuringSetup: true)
 input("gatewayPort", "text", title: "Gateway Port", required: true, displayDuringSetup: true)
}


def message(msg){
 log.debug(msg)
}

def refresh(){
 message("Executing 'refresh'")
 executeCommand("status")
}




// handle commands
def on() {
	message("Executing 'on'")
	executeCommand("on")    
}

def off() {
 	message("Executing 'off'")
	executeCommand("off")    
}

def hubActionResponse(response){

 def data = response.json ?: ""
 message("response: ${data}")
 def status = data?.result ?: "";

 message("switch status: '${status}'")

 if (status != "") {
     sendEvent(name: "switch", value: status == 'On' ? 'on' : 'off', isStateChange: true)
 }

}

def poll(){
  message("Executing 'poll'")
  executeCommand("status")
}

private executeCommand(command){

	
   def gatewayIPHex = convertIPtoHex(gatewayIP)
   def gatewayPortHex = convertPortToHex(gatewayPort)
   def headers = [:] 
   headers.put("HOST", "$gatewayIPHex:$gatewayPortHex")    

   message(headers)
   def path =  "/relay/$command?device=$deviceId"

   try {
   
   def action = 
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



private String convertIPtoHex(ipAddress) {
   String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
   return hex
}



private String convertPortToHex(port) {
   String hexport = port.toString().format( '%04x', port.toInteger() )
   return hexport
}