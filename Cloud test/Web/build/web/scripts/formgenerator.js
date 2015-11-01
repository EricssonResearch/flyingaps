var $xml;
var $messages;
var $enums;
var $version;

function startUp(){
    /* Read and parse XML into $xml variable */
    var xmlDir = 'scripts/common.xml';
    var stringData = $.ajax({
                    url: xmlDir,
                    async: false
                 }).responseText;
    var xmlDoc = $.parseXML( stringData); 
    $xml = $(xmlDoc);
    
    /* Extract messages, version and enums for convenience */
    $messages = $xml.find("messages").children();
    $enums = $xml.find("enums");
    $version = $xml.find("version");
    /* Generate form from xml */
    generateForm();
    
    /* Set description to match command on index 0 the first time page loads*/
    commandSelectedCallback(0);
}

/* Generates HTML form */
function generateForm(xmlDir){
    generateCommandSelect();
    /* Generate fields for command on index zero the first time page loads */
    generateCommandFields(0);
}

/* Generates the commands select options */
function generateCommandSelect(){
    $command_sel = $("#command_select");
    $messages.each(function(index,element){
        var newOpt = document.createElement("option");  // Create with DOM
        newOpt.innerHTML = $(element).attr("name");
        $command_sel.append(newOpt);
    });
}
/* Updates the description div whenever a new command is selected*/
function commandSelectedCallback(idx){
    var $command_desc = $("#command_description");
    $command_desc.show();
    var desc = $messages.eq(idx).find("description").text()
    $command_desc.html(desc);
    generateCommandFields(idx);
}

/* Generates the fields to be filled and sent */
function generateCommandFields(idx){
    var $parametersField = $("#parameters");
    var $fields = $messages.eq(idx).find("field");
    if($fields.length < 1){
        $("#parameters_fieldset").hide();
    }else{
        $("#parameters_fieldset").show();
        $parametersField.html("");
        $fields.each(function(index,element){
            $parametersField.append(generateFieldElement(element));
        });
    }
}
function generateFieldElement(element){
    var newField;
    var $element = $(element);
    var type = $element.attr("type");
    var parentDiv = document.createElement("div");
    /* Create and append label */
    var label = document.createElement("label");
    label.innerHTML = $element.attr("name")+ "&nbsp;";
    parentDiv.appendChild(label);
    /* Find out whether the command has an existing ENUM */
    var re = /MAV_([a-zA-Z]|_)*/;
    var match = $element.text().match(re);
    if(match!==null){
        var fieldEnum = match[0];
        var $enumList = $enums.find("[name="+fieldEnum+"]");
        
        var newSel = document.createElement("select");
        var $entries = $enumList.find("entry");
        $enumList.children().each(function(){
            if(this.hasAttribute("name")){
                var newOpt = document.createElement("option");
                newOpt.innerHTML = $(this).attr("name");
                newOpt.value=$(this).attr("value");
                newSel.appendChild(newOpt);
            }
        });
        newField = newSel;
    }else{
        switch(type) {
            //type="number" min="0" step="1"
            case "int8_t":
            case "int16_t":
            case "int32_t":
                newField  = document.createElement("input");  // Create with DOM
                newField.type = "number";
                newField.step=1;
                break;
            case "uint8_t":
            case "uint16_t":
            case "uint32_t":
                newField  = document.createElement("input");  // Create with DOM
                newField.type = "number";
                newField.min=0;
                newField.step=1;
                break; 
            case "uint8_t_mavlink_version":
                newField  = document.createElement("input");  // Create with DOM
                newField.value = $version.text();
                newField.readOnly = true;
                break;
            default:
               newField  = document.createElement("textarea");  // Create with DOM

        }
    }
    newField.id = $(element).attr("name");
    parentDiv.appendChild(newField);
    var description = document.createElement("span");
    description.innerHTML = $element.text();
    description.className = "description";
    parentDiv.appendChild(description);
    return parentDiv;
}