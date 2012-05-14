//disable async for loading the needed client scripts
$.ajaxSetup({cache: true, async: false });
jQuery.getScript('js/notifier.js');
jQuery.getScript('js/handlebars.js');
jQuery.getScript('js/wst/wstLog.js');
jQuery.getScript('js/wst/wstAPI.js');
jQuery.getScript('js/wst/wstTemplate.js');
jQuery.getScript('js/wst/wstTab.js');
$.ajaxSetup({cache: false, async: true });


(function(){
	
	
	/** GENERAL INITIALIZATION */

	//init tabbing
	var tab_div = $('#tabs');
	tab_div.wstTab();
	$('#test_button').on('click',function(){
		tab_div.wstTab('add',12,'name','html');
	});
	
	//initialize the contact list
	wstAPI.getContacts(generate_contact_list);
		
	//set the interval for updating the webapp
	this.timerId = setInterval(function(){
		var inst = this;
		wstAPI.pollInfo(update_webapp);
	},4000);
	
	
	
	/** LISTENERS */
	$('#contact_list').on('click','div',function(){
		var contact = $(this).data('contactFull');
		organize_contact_tab(contact);
	});

	
	tab_div.on('click','.contact_tab_close',function(){
		var id = $(this).data('contactId');
		if(tab_div.wstTab('remove',id)){
			wstLog.info('Contact-Tab successfully closed. To re-open it click on the entry in the contact list again.')
		}
	});
	
	
	
	/** WEBAPP METHODS */
	function update_webapp(json){
		
		if(json.contact_changed === true){
			wstLog.log('Contacts changed - updating contact list.');
			generate_contact_list(json);
		}
		
		wstLog.log('Updating webapp');
	}
	
	
	
	
	function organize_contact_tab(contact_json){
		var contact_tab = $('#contact_tab_'+contact_json.id);
		if(contact_tab.length) { //legal js way to evaluate element exists
			tab_div.wstTab('show_tab',contact_json.id)
		} else {
			var contact_name = contact_json.name+' '+contact_json.last_name;
			var html = wstTemplate.get('contact_tab',contact_json);
			tab_div.wstTab('add',contact_json.id,contact_name,html);
			wstAPI.fetchSMSThread(contact_json.id, generate_sms_thread_list);
		}
	}
	
	
	function generate_sms_thread_list(json){
		if(json != null){
			var contact_id = json.contact_id;
			var thread_length = json.thread_messages.length;
			if(thread_length > 0){
				var html = '';
				for(var i = 0; i < thread_length; i++){
					html += wstTemplate.get('sms_thread_entry', json.thread_messages[i])+'\n';
				}
				$('#sms_thread_'+contact_id).html(html);
				wstLog.success('Thread-List successfully loaded.');
				return true;
			} else {
				wstLog.info('No sms stored for this contact.');
				$('#sms_thread_'+contact_id).html("");
				return true;
			}
		}
		wstLog.warn('SMS-Thread-List could not be loaded.');
		return false;
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
				wstLog.success('Contact-List successfully updated.');
				return true;
			}
		}
		wstLog.warn('Contact-List could not be loaded.');
		return false;
	}

	
	
})();