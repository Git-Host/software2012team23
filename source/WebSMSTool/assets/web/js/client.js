//disable async for loading the needed client scripts
$.ajaxSetup({cache: true, async: false });
jQuery.getScript('js/wst/wstLog.js');
jQuery.getScript('js/wst/wstAPI.js');
jQuery.getScript('js/handlebars.js');
jQuery.getScript('js/wst/wstTemplate.js');
$.ajaxSetup({cache: false, async: true });