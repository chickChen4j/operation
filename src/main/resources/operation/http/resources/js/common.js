$.namespace("druid.common");

druid.common = function () {
  var statViewOrderBy = '';
  var statViewOrderBy_old = '';
  var statViewOrderType = 'asc';
  var isOrderRequest = false;

  // only one page for now
  var sqlViewPage = 1;
  var sqlViewPerPageCount = 1000000;

  return {
    init: function () {
      this.buildFooter();
    },

    buildHead: function (index) {
      $.get('header.html', function (html) {
        $(document.body).prepend(html);
        $(".navbar .nav li").eq(index).addClass("active");
      }, "html");

    },

    buildFooter: function () {

      var html = '<footer class="footer">' +
          '<div class="container" style="text-align: center">' +
          // '<a href="https://render.alipay.com/p/s/taobaonpm_click/druid_banner_click" target="new"><img src="https://render.alipay.com/p/s/taobaonpm_click/druid_banner"></a><br/>' +
          //   '	powered by <a href="https://github.com/alibaba/" target="_blank">Alibaba</a> & sandzhang & <a href="http://melin.iteye.com/" target="_blank">melin</a> & <a href="https://github.com/shrekwang" target="_blank">shrek.wang</a>'+
          //   '			</div>'+
          // ' </footer>';
          'Copyright&copy;2019 5Star 研发交付部' +
          '</div>' +
          '</footer>';
      $(document.body).append(html);
    },



    getAjaxUrl: function (uri) {
      var result = uri;

      if (statViewOrderBy != undefined) {
        result += 'orderBy=' + statViewOrderBy + '&';
      }

      if (statViewOrderType != undefined) {
        result += 'orderType=' + statViewOrderType + '&';
      }

      if (sqlViewPage != undefined) {
        result += 'page=' + sqlViewPage + '&';
      }

      if (sqlViewPerPageCount != undefined) {
        result += 'perPageCount=' + sqlViewPerPageCount + '&';
      }

      return result;
    },

    resetSortMark: function () {
      var divObj = document.getElementById('th-' + statViewOrderBy);
      var old_divObj = document.getElementById('th-' + statViewOrderBy_old);
      var replaceToStr = '';
      if (old_divObj) {
        var html = old_divObj.innerHTML;
        if (statViewOrderBy_old.indexOf('[') > 0) {
          replaceToStr = '-';
        }
        html = html.replace('▲', replaceToStr);
        html = html.replace('▼', replaceToStr);
        old_divObj.innerHTML = html
      }
      if (divObj) {
        var html = divObj.innerHTML;
        if (statViewOrderBy.indexOf('[') > 0) {
          html = '';
        }

        if (statViewOrderType == 'asc') {
          html += '▲';
        } else if (statViewOrderType == 'desc') {
          html += '▼';
        }
        divObj.innerHTML = html;
      }
      isOrderRequest = true;

      this.ajaxRequestForBasicInfo();
      return false;
    },

    setOrderBy: function (orderBy) {
      if (statViewOrderBy != orderBy) {
        statViewOrderBy_old = statViewOrderBy;
        statViewOrderBy = orderBy;
        statViewOrderType = 'desc';
        druid.common.resetSortMark();
        return;
      }

      statViewOrderBy_old = statViewOrderBy;

      if (statViewOrderType == 'asc') {
        statViewOrderType = 'desc'
      } else {
        statViewOrderType = 'asc';
      }

      druid.common.resetSortMark();
    },

    ajaxuri: "",
    handleCallback: null,
    handleAjaxResult: function (data) {
      druid.common.handleCallback(data);
      if (!isOrderRequest) {
        druid.lang.trigger();
      }
    },//ajax 处理函数
    ajaxRequestForBasicInfo: function () {
      $.ajax({
        type: 'POST',
        url: druid.common.getAjaxUrl(druid.common.ajaxuri),
        success: function (data) {
          druid.common.handleAjaxResult(data);
        },
        dataType: "json"
      });
    },

    subSqlString: function (sql, len) {
      if (sql == undefined || sql == null) {
        return '';
      }

      if (sql.length <= len) {
        return sql;
      }
      return sql.substr(0, len) + '...';
    },

    stripes: function () {
      $("#dataTable tbody tr").each(function () {
        $(this).removeClass("striped");
      });
      $("#dataTable tbody tr:even").each(function () {
        $(this).addClass("striped");
      });
    },

    getUrlVar: function (name) {
      var vars = {};
      var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi,
          function (m, key, value) {
            vars[key] = value;
          });
      return vars[name];
    }
  }
}();

$(document).ready(function () {
  druid.common.init();
});

function replace(data) {
  if ((!data) || data === undefined) {
    return '';
  } else {
    return format(data);
  }
}

function format(s) {
  var str = s += '';
  return str.replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,");
}
