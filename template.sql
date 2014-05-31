select
<% def comment(value, space = 2) {
     return value.empty ? '' : ' ' * space + '-- ' + value
   }

   binding['selects'].eachWithIndex{s, i -> 
%>\
  <%= i == 0 ? '' : ', ' %><%= s[0] %>.<%= s[2] %><%= comment(s[3], (i == 0 ? 4 : 2)) %>
<% } %>\
from <%= binding['from'][0] %><%= comment(binding['from'][1]) %>
<% binding['joins'].each{ %>\
left outer join <%= it[0] %><%= comment(it[1]) %>
  <% it[2].eachWithIndex{on, j -> %>\
  <%= j == 0 ? 'on  ' : 'and ' %><%= it[0] + '.' + on[1] %> = <%= on[0] +'.' + on[1] %><%= comment(on[2]) %>
  <% } %>\
<% } %>\
