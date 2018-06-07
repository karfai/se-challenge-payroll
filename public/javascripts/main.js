$(document).ready(function () {
  $('#modal-upload').modal('attach events', '#button-show-modal', 'show');
  let vm = {
    report: ko.observableArray([])
  };

  $.getJSON("/time_sheets", function (report) {
    console.log(report);
    vm.report(_.map(_.get(report, "totals"), function (o) {
      return {
        name: o.pay.name,
        total: o.pay.total,
        period: `${o.starts} - ${o.ends}`,
        groups: _.join(o.pay.groups, ", ")
      };
    }));
  });
  
  ko.applyBindings(vm, document.getElementById('report-contents'));
});
