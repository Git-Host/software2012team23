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
	
	
	Handlebars.registerHelper('set_person', function(sms_json) {
		//lookup if contact is in contact list for fetching data attribute
		var contact_entry = $('#contact_entry_'+sms_json.person);
		if(contact_entry.length){
			var contact_data = contact_entry.data('contactFull');
			return contact_data.name+' '+contact_data.last_name;
		} else {
			return sms_json.address;
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