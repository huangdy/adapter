import React from "react";
import { Component } from "react";
import { Progress } from "reactstrap";
import axios from "axios";
import "./FileUpload.css";
import SelectBox from "../features/select-box";

class FileUpload extends Component {
    constructor(props) {
        super(props);
        this.state = {
            selectedFile: null,
            configurations: null,
            isConfig: this.props.type === "csv" ? false : true,
            csvConfigurationName: "cvs",
            listCSVConfigurationName: []
        };
        if (!this.state.isConfig) this.getCSVConfigurationName();
    }

    onSelectCSVConfig(name) {
        this.setState({ csvConfigurationName: name });
    }

    getCSVConfigurationName() {
        axios.get("http://localhost:8088/api/listCSVConfigurationName").then(res => {
            var list = [];
            for (var i = 0; i < res.data.length; i++) list[i] = { value: res.data[i], id: i + 1 };
            // console.log("listCSVConfigurationName: ", list);
            this.setState({ listCSVConfigurationName: list });
        });
    }

    componentWillReceiveProps() {
        this.setState.isConfig = this.props.type === "csv" ? false : true;
        if (!this.state.isConfig) {
            this.getCSVConfigurationName();
        } else {
            this.setState({ listCSVConfigurationName: [] });
        }
    }

    onChangeHandler = event => {
        this.setState({
            selectedFile: event.target.files,
            loaded: 0
        });
    };

    onClickHandler = () => {
        const data = new FormData();
        for (var i = 0; i < this.state.selectedFile.length; i++) {
            data.append("files", this.state.selectedFile[i]);
        }

        if (this.state.isConfig) {
            axios
                .post("http://localhost:8088/api/uploadMultiConfig", data, {
                    onUploadProgress: ProgressEvent => {
                        this.setState({
                            loaded: (ProgressEvent.loaded / ProgressEvent.total) * 100
                        });
                    }
                })
                .then(res => {
                    // then print response status
                    console.log(res);
                });
        } else {
            data.append("configuration_name", "cvs");
            axios
                .post("http://localhost:8088/api/uploadMultiCSVFile", data, {
                    onUploadProgress: ProgressEvent => {
                        this.setState({
                            loaded: (ProgressEvent.loaded / ProgressEvent.total) * 100
                        });
                    }
                })
                .then(res => {
                    // then print response status
                    console.log(res);
                });
        }
    };

    render() {
        return (
            <div class='container'>
                <div class='row'>
                    <div class='col-md-6'>
                        <form method='post' action='#' id='#'>
                            <div class='form-group files'>
                                <label>Upload {this.state.isConfig ? "Configuration" : "CSV"} File(s)</label>
                                <input type='file' class='form-control' multiple onChange={this.onChangeHandler} />
                            </div>
                        </form>
                        <div class='form-group'>
                            <Progress max='100' color='success' value={this.state.loaded}>
                                {Math.round(this.state.loaded, 2)}%
                            </Progress>
                        </div>
                        <button type='button' class='btn btn-success btn-block' onClick={this.onClickHandler}>
                            Upload
                        </button>
                    </div>
                </div>
                <div>
                    {this.state.listCSVConfigurationName.length === 0 ? null : (
                        <div class='row'>
                            <div class='col-md-6'>
                                <div>
                                    <div style={{ margin: "16px", position: "relative" }}>
                                        <h3>CSV Configuration</h3>
                                        <SelectBox items={this.state.listCSVConfigurationName} />
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        );
    }
}

export default FileUpload;
