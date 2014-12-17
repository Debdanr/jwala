/**
 * A button meant purely to be used in a React way (e.g. don't use by rendering this component to string then using jQuery to update the DOM) since it maintains state.
 * This component will eventually replace the GenericButton.
 *
 * Properties
 *
 * 1. className - the class applied to the button. If not specified jQuery UI theme classes for regular buttons are used.
 * 2. hoverClassName - the hover class applied to the button on mouse over. If not specified jQuery UI theme classes for regular buttons are used.
 * 3. spanClassName - the class applied to the span component of the button that contains the label. If not specified the jQuery UI theme classes for regular buttons are used.
 * 4. onClick - the callback that is executed when the button is clicked.
 * 5. label - the label of the button.
 * 6. title - the title of the button that is shown in a tooltip when the user hovers the mouse pointer over it.
 */
var RButton = React.createClass({
    getInitialState: function() {
        return {
            hover: false
        }
    },
    render: function() {
        var className = (this.props.className !== undefined) ? this.props.className : "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only";
        var spanClassName = (this.props.spanClassName !== undefined) ? this.props.spanClassName : "ui-button-text";
        var theHoverClassName = (this.props.hoverClassName !== undefined) ? this.props.hoverClassName : "ui-state-hover";
        var hoverClassName = this.state.hover ? theHoverClassName : "";
        return React.DOM.button({className: className + " " + hoverClassName,
                                 title:this.props.title,
                                 type:"button",
                                 role:"button",
                                 ariaDisabled:false,
                                 onClick:this.handleClick,
                                 onMouseOver:this.mouseOverHandler,
                                 onMouseOut:this.mouseOutHandler},
                                 React.DOM.span({className:spanClassName}, this.props.label));
    },
    handleClick: function() {
        this.props.onClick();
    },
    mouseOverHandler: function() {
        this.setState({hover:true});
    },
    mouseOutHandler: function() {
        this.setState({hover:false});
    },
});