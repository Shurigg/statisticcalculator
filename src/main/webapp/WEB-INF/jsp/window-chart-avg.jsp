<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript">
window.onload = function() {

var dps = [];
var chart = new CanvasJS.Chart("chartContainer", {
	animationEnabled : true,
	zoomEnabled: true,
	theme : "light1",
	title : {
		text : "Best window and frame for ${protocol} packets"
	},
	axisY : {
		title : "Average length of packet",
		labelFontSize: 15,
		labelFormatter : addSymbols
	},
    axisX : {
        title : "Frame size: ${frameSize}   Window size: ${bestWindowSize} frames",
        labelFontSize: 15,
        labelAngle: 70
    },
	data : [ {
		type : "area",
		markerSize : 0,
		yValueFormatString : "#,##0 bytes",
		dataPoints : dps
	} ]
});

var yValue;
var label;

<c:forEach items="${chartData}" var="data">
	yValue = parseFloat("${data.value}");
	label = "${data.label}";
	dps.push({
		label : label,
		y : yValue,
	});
</c:forEach>

chart.render();

function addSymbols(e) {
	var suffixes = [ "", "K", "M", "B" ];

	var order = Math.max(
			Math.floor(Math.log(e.value) / Math.log(1000)), 0);
	if (order > suffixes.length - 1)
		order = suffixes.length - 1;

	var suffix = suffixes[order];
	return CanvasJS.formatNumber(e.value / Math.pow(1000, order)) + suffix;
}

}
</script>
</head>
<body>
<div id="chartContainer" style="height: 500px; width: 100%;"></div>
<script src="https://canvasjs.com/assets/script/canvasjs.min.js"></script>
</body>
</html>