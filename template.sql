select
<% binding['selects'].eachWithIndex{s, i -> %>\
  <%= i == 0 ? '' : ', ' %><%= s[0] %>.<%= s[2] %><%= s[3].empty ? '' : (i == 0 ? '    --' : '  --') + s[3] %>
<% } %>\
from <%= binding['from'][0] %>
<% binding['joins'].each{ %>\
left outer join <%= it[0] %><%= it[1].empty? '' : '  --' + it[1] %>
  <% it[2].eachWithIndex{on, j -> %>\
  <%= j == 0 ? 'on  ' : 'and ' %><%= it[0] + '.' + on[1] %> = <%= on[0] +'.' + on[1] %><%= on[2].empty? '' : '  --' + on[2] %>
  <% } %>\
<% } %>\
