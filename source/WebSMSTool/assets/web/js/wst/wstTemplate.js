(function(window) {
	wstLog.log('Load wst template engine.');
	
    var wstTemplate = {
    		templateMap:{},
    		fetchTemplate: function(name){
    			var template = null;
    			var response = null;
    			 $.get('js/wst/templates/'+name+'.html',function(resp){
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
    
})(window);