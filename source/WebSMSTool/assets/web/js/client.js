//disable async for loading the needed client scripts
$.ajaxSetup({cache: true, async: false });
jQuery.getScript('js/wst/wstLog.js');
jQuery.getScript('js/wst/wstAPI.js');
jQuery.getScript('js/handlebars.js');
jQuery.getScript('js/wst/wstTemplate.js');
jQuery.getScript('js/wst/wstTab.js');
$.ajaxSetup({cache: false, async: true });


(function(){
	
	
	Handlebars.registerHelper('json_string', function(full_json) {
		  return JSON.stringify(full_json);
	});	
	
	Handlebars.registerHelper('full_name', function(person) {
		  return person.name+' '+person.last_name;
	});		
	
	
	$('#tabs').wstTab();
	$('#test_button').on('click',function(){
		$('#tabs').wstTab('add',12,'name','html');
	});
	
	wstAPI.getContacts(generate_contact_list);
	
	
	function generate_contact_list(json){
		wstLog.log(json);
		if(json != null){
			var cl_length = json.contacts.length;
			if(cl_length > 0){
				var html = '';
				for(var i = 0; i < cl_length; i++){
					html += wstTemplate.get('contact_entry', json.contacts[i])+'\n';
				}
				$('#contact_list').html(html);
				return true;
			}
		}
		return false;
	}
})();