/** @jsx React.DOM */
var LoginArea = React.createClass({
    getInitialState: function() {
        return {
            error: ""
        }
    },
    render: function() {
        return <div className={this.props.className}>
                    <form id="logInForm">
                       <br/>
                       <MessageLabel msg={this.state.error} className="login-error-msg"/>
                       <span className="title">TOMCAT</span><br/>
                       <div className="gear-position">
                            <img src="public-resources/img/react/gear.gif"/>
                       </div>
                       <span className="title margin-left-17px">PERATIONS</span><br/>
                       <span className="title">CENTER</span>
                       <br/>
                       <br/>
                       <TextBox id="userName" name="userName" className="input" hint="User Name" hintClassName="hint"/>
                       <br/>
                       <TextBox id="password" name="password" isPassword={true} className="input" hint="Password"
                                hintClassName="hint" onKeyPress={this.passwordTextKeyPress}/>
                       <br/>
                       <input type="button" value="Log In" onClick={this.logIn} />
                   </form>
               </div>
    },
    passwordTextKeyPress: function(event) {
        if (event.keyCode === 13) {
            this.logIn();
        }
    },
    logIn: function() {
        // TODO: Refactor to use dynamic state update to make this more inline with React.
        // NOTE! You might have to modify TextBox component for to Reactify this.
        if (!$("#userName").val().trim() || !$("#password").val()) {
            this.setState({error:"User name and password are required."});
        } else {
            userService.login($("#logInForm").serialize(), this.successCallback, this.errorCallback);
        }
    },
    successCallback: function() {
        window.location = window.location.href.replace("/login", "");
    },
    errorCallback: function(e) {
        if (e !== undefined && e !== null && e.indexOf("error code 49") > -1) {
            this.setState({error:"Your user name or password is incorrect."});
        } else {
            this.setState({error:e});
        }
    }
});

$(document).ready(function(){
    var errorMessage = tocVars.loginStatus === "error" ? "Your user name or password is incorrect.": "";
    React.renderComponent(<LoginArea className="login-area" error={errorMessage}/>, document.body);
});