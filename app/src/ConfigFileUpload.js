import React from "react";
import { Component } from "react";
import { Progress } from "reactstrap";
import axios from "axios";
import "./FileUpload.css";

class FileUpload extends Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedFile: null
    };
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
      data.append("file", this.state.selectedFile[i]);
    }

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
        console.log(res.statusText);
      });
  };

  render() {
    return (
      <div class="container">
        <div class="row">
          <div class="col-md-6">
            <form method="post" action="#" id="#">
              <div class="form-group files">
                <label>Upload Configuration File(s)</label>
                <input
                  type="file"
                  class="form-control"
                  multiple
                  onChange={this.onChangeHandler}
                />
              </div>
            </form>
            <div class="form-group">
              <Progress max="100" color="success" value={this.state.loaded}>
                {Math.round(this.state.loaded, 2)}%
              </Progress>
            </div>
            <button
              type="button"
              class="btn btn-success btn-block"
              onClick={this.onClickHandler}
            >
              Upload
            </button>
          </div>
        </div>
      </div>
    );
  }
}

export default FileUpload;
