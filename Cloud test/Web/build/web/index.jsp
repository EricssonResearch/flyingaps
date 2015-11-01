<%-- 
    Document   : index
    Created on : Oct 28, 2015, 10:41:38 PM
    Author     : Vyz
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link type="text/css" rel="stylesheet" href="css/main.css">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <script src="scripts/formgenerator.js"></script>
        <title>JSP Page</title>
       
    </head>
    <body onload="startUp();">
        <div id="container">
            <h1>Flying Access Points App Ground Control</h1>
            <hr/>
            <div id="command_table">
                <form action="index.jsp" >
                    <fieldset id="config_fieldset">
                        <legend>App Config:</legend>
                        <label for="api_key">API Key</label>
                        <input id="api_key" type="text" maxlength="39" size="39"/>&nbsp;
                        <label for="sender_id">Sender ID</label>
                        <input id="sender_id" type="text" maxlength="12" size="12"/>
                    </fieldset>
                    <fieldset id="command_fieldset">
                        <legend>Command:</legend>
                        <select id="command_select" onchange="commandSelectedCallback(this.selectedIndex)">
                        </select>
                    </fieldset>
                    <fieldset id="parameters_fieldset">
                        <legend>Parameters:</legend>
                        <div id="parameters">
                            
                        </div>
                    </fieldset>
                    <fieldset id="description_fieldset">
                        <legend>Description</legend>
                        <div id="command_description" hidden="true"></div>
                    </fieldset>
                    <br/><hr/>
                    <div id="submit_button">
                    <input type="submit" value="Submit GCM">
                    </div>
                </form>
            </div>
        </div>
    </body>
</html>
