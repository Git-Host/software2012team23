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
	wstLog.log('Load wst template engine.');
	
    var wstTemplate = {
    		templateMap:{},
    		fetchTemplate: function(name){
    			var template = null;
    			var response = null;
    			 $.get('js/wst/templates/'+name+'.html',function(resp){  //async is on while initialization of the wst stuff
    	    		 wstLog.log('Try to load template with name: '+name);
    	    		 response = resp;
    			 });
    			 template = Handlebars.compile(response);
				 this.templateMap[name] = template;
				 wstLog.log('Html compiled and added to map.');
    		},
    		init : function(){
    			wstLog.log('Init wst templating');
    			this.fetchTemplate('test');
    			this.fetchTemplate('contact_entry');
    			this.fetchTemplate('sms_thread_entry');
    			this.fetchTemplate('contact_tab');
    		},
    		get : function(templateName, data){
    			wstLog.log('Try to return template '+templateName);
    			if(this.templateMap[templateName] != null){
    				wstLog.log('Template '+templateName+' found.');
    				var templateFn = this.templateMap[templateName];
    				return templateFn(data);
    			}
    		}
    };
    
    wstTemplate.init();
    window.wstTemplate = wstTemplate;
    
    
    
    
	Handlebars.registerHelper('json_string', function(full_json) {
		  return JSON.stringify(full_json);
	});	
	
	
	Handlebars.registerHelper('full_name', function(person) {
		  return person.name+' '+person.last_name;
	});
	
	
	Handlebars.registerHelper('set_person', function(contact_id) {
		//lookup if contact is in contact list for fetching data attribute
		var contact_entry = $('#contact_entry_'+contact_id);
		if(contact_entry.length){
			var contact_data = contact_entry.data('contactFull');
			return contact_data.name+' '+contact_data.last_name;
		} else {
			//return sms_json.address;
			return "ME";
		}
	});	
	
	Handlebars.registerHelper('set_contact_form_input', function(contact_numbers) {
		var html = '';
		if(contact_numbers.length > 1){
			html += "<select id=\"number\" name=\"number\" required>";
			for(var i = 0; i < contact_numbers.length; i++){
				html += "<option value=\""+contact_numbers[i].number+"\">"+contact_numbers[i].number+"</option>";
			}
			html += "</select>";
		} else {
			html = "<input type=\"text\" name=\"number\" id=\"number\" size=\"70\" placeholder=\"Enter number or double click on contact\" required/>";
		}  
		
		return new Handlebars.SafeString(html);
	});	   	
    
})(window);