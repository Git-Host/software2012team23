/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
(function(window) {
    var wstLog = {
    	debug : true,
        log: function(message){
            if(console && this.debug){
                console.log(message);            
            }
        },
    
    	notify: function(type,message){
    		if(type == "success"){
    		    Notifier.success(message, 'SUCCESS');    			
    		} else if(type == "warn"){
    			Notifier.warning(message, 'WARNING');
    		} else if(type == "error"){
    			Notifier.error(message, 'ERROR');
    		} else if(type == "info"){
    			Notifier.info(message, 'INFO');
    		}
    	},
    	
    	success: function(message){
    		this.notify('success',message);
    	},
    	warn: function(message){
    		this.notify('warn', message);
    	},
    	error: function(message){
    		this.notify('error', message);
    	},
    	info: function(message){
    		this.notify('info', message);
    	}
    
    };
    window.wstLog = wstLog;
})(window);
