$(document).ready(function () {
  let report_vm = {
    report: ko.observableArray([])
  };

  report_vm.update = function() {
    report_vm.report([]);
    $.getJSON("/time_sheets", function (report) {
      report_vm.report(_.map(_.get(report, "report"), function (o) {
        return {
          name: o.pay.name,
          total: o.pay.total,
          period: `${o.starts} - ${o.ends}`,
          groups: _.join(o.pay.groups, ", ")
        };
      }));
    });
  };
  
  let upload_vm = {
    file: ko.observable(),
    error_message: ko.observable()
  };

  upload_vm.keep_file = function (vm, evt) {
    console.log(_.head(evt.target.files));
    upload_vm.file(_.head(evt.target.files));
    upload_vm.error_message("");
  };
  
  upload_vm.report_error = function (err) {
    if (err.status === 'report_exists') {
      upload_vm.error_message(`Report #${err.args.id} has already been uploaded`);
    }
  };

  upload_vm.upload = function () {
    var d = new FormData();
    d.append("content", upload_vm.file());
    var req = new XMLHttpRequest();
    req.open("POST", "/time_sheets", true);
    req.setRequestHeader("Csrf-Token", "nocheck");
    req.onload = function () {
      if (req.readyState === 4) {
        if (req.status === 200) {
          report_vm.update();
        } else {
          upload_vm.report_error(JSON.parse(req.response));
        }
      }
    };
    
    req.send(d);
  };

  upload_vm.have_file = ko.computed(function () {
    return !!upload_vm.file();
  });

  upload_vm.no_errors = ko.computed(function () {
    return _.size(upload_vm.error_message()) == 0;
  });

  upload_vm.file_label = ko.computed(function () {
    let file = upload_vm.file();
    return (file && _.size(file.name) > 0) ? file.name : "Select file";
  });
  
  ko.applyBindings(report_vm, document.getElementById('report-contents'));
  ko.applyBindings(upload_vm, document.getElementById('upload'));

  report_vm.update();
});
