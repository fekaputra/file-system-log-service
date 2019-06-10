function sendIt() {
    var url = 'http://localhost:8080/search';
    var request = {"pathname": document.getElementById("pathname").value};

    $.ajax({
        type: "POST",
        url: url,
        data: JSON.stringify(request),
        dataType: "json",
        contentType: "application/json"
    }).then(function (response) {
        var json = response;
        // console.log(json);
        var nodelabel = [];
      //  var nodes = [{id: "start", label: 'start'}, {id: "end", label: 'end'}];
        var nodes = [];

        var edges = [];
        for (var i = 0; i < json.length; i++) {
            var target = json[i].tfilename.value;

            var contained1 = (nodelabel.indexOf(target) > -1);
            if(!contained1){
                nodelabel.push(target);
                nodes.push({id: target, label: target});
            }

            var contained2 = (nodelabel.indexOf(json[i].filename.value) > -1);
            if(!contained2){
                nodelabel.push(json[i].filename.value);
                nodes.push({id: json[i].filename.value, label: json[i].filename.value});
            }

            edges.push({
                time: json[i].time.value,
                from: json[i].filename.value,
                user: json[i].user.value,
                program: json[i].program.value,
                to: target,
                arrows: 'to',
                label: json[i].fileAccess.value,
                font: {align: 'middle'}
            });
        }
        console.log(nodes);
        console.log(edges);
        //  console.log(nodelabel);

        // create a network
        var container = document.getElementById('mynetwork');
        var data = {
            nodes: nodes,
            edges: edges
        };
        var options = {physics: false};
        var network = new vis.Network(container, data, options);

        network.on("click", function (params) {
            params.event = "[original event]";
            var edgeId = params.items[0].edgeId;
            console.log("cicked edge with id: " + edgeId);
            const result = data.edges.filter(edge => edge.id === edgeId);
            console.log(result);
            /*document.getElementById('eventSpan').innerHTML = '<h3>Clicked edge:</h3><' +
                'p>time: ' + result[0].time + '\n'+'access event: '+ result[0].label +
                '</p>\n<h2>Click event:</h2>' + JSON.stringify(params, null, 4);*/
            fillEdgeInfo(result);
        });
    });

    function fillEdgeInfo(result) {
        var d = document.getElementById('edgeInfo');
        d.innerHTML = '<div style="padding-left: 10px;padding-top: 10px;">' +
            '<h5>Event - \'<b>' + result[0].label + '</b>\'</h5>' +
            '<p style="font-size: 12px;">' +
            //  '<b>Access Event: </b>' + result[0].label  +
            '<br /><b>Timestamp: </b>' + result[0].time +
            '<br /><b>From: </b>' + result[0].from +
            '<br /><b>To: </b>' + result[0].to +
            '<br /><b>User: </b>' + result[0].user +
            '<br /><b>Program: </b>' + result[0].program +
            /*  '<table style="table-layout:fixed;width:100%;font-family: arial, sans-serif;border-collapse: collapse;">' +
              '<tr><th style="border: 1px solid #dddddd;padding: 8px;text-align: left;">Property</th><th>Value</th></tr>' +
              '<tr><td style="border: 1px solid #dddddd;padding: 8px;text-align: left;word-wrap:break-word;overflow-wrap:break-word;">From</td><td>' + result[0].from + '</td></tr>' +
              '<tr><td style="border: 1px solid #dddddd;padding: 8px;text-align: left;word-wrap:break-word;overflow-wrap:break-word;">To</td><td>' + result[0].to + '</td></tr>' +
              '<tr><td style="border: 1px solid #dddddd;padding: 8px;text-align: left;word-wrap:break-word;overflow-wrap:break-word;">Time</td><td>' + result[0].time + '</td></tr>' +
              '<tr><td style="border: 1px solid #dddddd;padding: 8px;text-align: left;word-wrap:break-word;overflow-wrap:break-word;">User</td><td>' + result[0].user + '</td></tr>' +
              '<tr><td style="border: 1px solid #dddddd;padding: 8px;text-align: left;word-wrap:break-word;overflow-wrap:break-word;">Program</td><td>' + result[0].program + '</td></tr>' +
              '</table>' +*/
            '</p></div>';
    }
}