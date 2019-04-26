import React from "react";
import { Component } from "react";
import axios from "axios";
import "./FileUpload.css";
import SelectBox from "../features/select-box";

class Query extends Component {

    constructor(props) {
        super(props);
        this.state = {
            configurationName: "cvs",
            listConfigurationName: []
        };
        this.getConfigurationName();
    }

    onSelectConfiguration(name) {
        this.setState({ configurationName: name });
    }

    getConfigurationName() {
        axios.get("http://localhost:8088/api/listConfigurationName").then(res => {
            var list = [];
            for (var i = 0; i < res.data.length; i++)
                list[i] = {
                    value: res.data[i],
                    id: i + 1
                };
            console.log("listConfigurationName: ", list);
            this.setState({ listConfigurationName: list });
        });
    }

    render() {
        return (
            <div class='container'>
                <div class='row'>
                    <div class='col-md-6'>
                        <div>
                            <div style={{ margin: "16px", position: "relative" }}>
                                <h3>Configuration Name</h3>
                                <SelectBox items={this.state.listConfigurationName} />
                            </div>
                        </div>
                        <div>

                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default Query;