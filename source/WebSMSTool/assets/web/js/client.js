//disable async for loading the needed client scripts
$.ajaxSetup({cache: true, async: false });
jQuery.getScript('js/wst/wstLog.js');
jQuery.getScript('js/wst/wstAPI.js');
jQuery.getScript('js/handlebars.js');
jQuery.getScript('js/wst/wstTemplate.js');
jQuery.getScript('js/wst/wstTab.js');
$.ajaxSetup({cache: false, async: true });


(function(){
	
	var tab_div = $('#tabs');
	
	//init tabbing
	tab_div.wstTab();
	$('#test_button').on('click',function(){
		tab_div.wstTab('add',12,'name','html');
	});
	
	wstAPI.getContacts(generate_contact_list);
	
	$('#contact_list').on('click','div',function(){
		var contact = $(this).data('contactFull');
		organize_contact_tab(contact);
		return false;
	});
	
		
	function organize_contact_tab(contact_json){
		var contact_tab = $('#contact_tab_'+contact_json.id);
		if(contact_tab.length) { //legal js way to evaluate element exists
			tab_div.wstTab('show_tab',contact_json.id)
		} else {
			var contact_name = contact_json.name+' '+contact_json.last_name;
			var html = wstTemplate.get('contact_tab',contact_json);
			tab_div.wstTab('add',contact_json.id,contact_name,html);
		}
	}
	
	
	
	
	function generate_contact_list(json){
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