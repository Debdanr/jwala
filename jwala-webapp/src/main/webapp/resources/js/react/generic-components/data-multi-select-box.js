var DataMultiSelectBox = React.createClass({
    /**
     * Returns a value based on the dataField.
     * This method can return values whose dataField are 2 levels deep e.g. (id.id, name.lastName).
     */
    getVal: function(data) {
        var fieldArray = this.props.dataField.split(".");
        if (fieldArray.length === 2) {
            return data[fieldArray[0]][fieldArray[1]];
        }
        return data[fieldArray[0]]
    },
    shouldComponentUpdate: function(nextProps, nextState) {
        return !nextProps.noUpdateWhen;
    },
    getInitialState: function() {
        return {checkBoxData: []};
    },
    componentWillReceiveProps: function(nextProps) {
        var checkBoxData = [];
        for (var i = 0; i < nextProps.data.length; i++) {
            var valId = this.getVal(nextProps.data[i]);
            checkBoxData.push({checked: this.getCheckedVal(nextProps.selectedValIds, valId), valId:valId});
        }
        this.setState({checkBoxData:checkBoxData});
    },
    getCheckedVal: function(selectedValIds, valId) {
        if (selectedValIds !== undefined) {
            for (var i = 0; i < selectedValIds.length; i++) {
                if (selectedValIds[i].id === valId) {
                    return "checked";
                }
            }
        }
        return "";
    },
    render: function() {
        var self = this;
        var options = [];
       for (var i = 0; i < this.props.data.length; i++) {
            var props = {name:this.props.name,type:"checkbox",
                         value:this.getVal(this.props.data[i]),
                         checked:this.state.checkBoxData[i].checked,
                         onChange:this.changeHandler.bind(this, i)};

            // We need to wrap the checkbox in a div to prevent this issue:
            // https://github.com/facebook/react/issues/997
            options.push(React.DOM.div(null, React.DOM.input(props, this.props.data[i][this.props.val])));
        }
        return React.DOM.div({className:this.props.className}, options);
    },
    changeHandler: function(i) {
        if (this.state.checkBoxData[i].checked === "") {
            this.state.checkBoxData[i].checked = "checked";
        } else {
            this.state.checkBoxData[i].checked = "";
        }
        this.setState({checkBoxData:this.state.checkBoxData});

        var selectedValIds = [];
        for (var i = 0; i < this.state.checkBoxData.length; i++) {
            if (this.state.checkBoxData[i].checked === "checked") {
                var obj = {id: this.state.checkBoxData[i].valId};

                // this is for the insert part since the rest service
                // post and get do not have the same name for the id
                obj[this.props.idKey] = this.state.checkBoxData[i].valId;

                selectedValIds.push(obj);
            }
        }

        this.props.onSelectCallback(selectedValIds);
    }
})