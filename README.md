

# README


## What Is This

-   This is a tool for randomly generating characters (NPCs or PCs), monsters - and just about anything that could be modelled in a RPG system.
-   Utilizing templates, lookup tables, and a random number generator, it is possible to automatically generate many of the entities that are present in conventional tabletop roleplaying games.


## Raison d'Etre

-   There are many fun and exciting roleplaying game systems. Some of these systems provide more detail and 'crunch' than others. The downside of 'crunch' is that it can distract from the narrative flow of a game session.
-   In the worst case, the work involved in supporting such a detailed set of rules causes a game to become a drab and boring exercise in bookkeeping - a 60-second combat takes three hours to resolve, players get bored waiting for the GM to roll up new NPCs, etc.
-   This tool is intended to make generating entities of any kind - NPCs, creatures, villages, vehicles, dungeons, etc quick and easy, so that the focus of the game remains on the story, but without having to give up the lush detail that a crunchy ruleset provides.


## Templates


### Format

-   Templates are defined in plain text .yaml format. Like any yaml document, a template has a tree structure. Each node has a value, which may either be a bit of text, or a subtree of nodes.


### Functionality

-   Template functionality is a little bit like that provided by XSLT/XPath (but much more limited), and allows for
    -   virtual random dicerolls
    -   external table lookup values
    -   references to specific values within the same template
-   However, namespaces and looping constructs are not supported.


### Syntax

-   Template syntax is yaml syntax (see <http://www.yaml.org> ), plus a few special escaped sequences
    -   Every template starts with the same structure - a root `template` node, and a child `template_name` node:
        
            template:
              template_name: the_name_of_the_template
    -   The rest of the template is comprised of nodes that hang off the root `template` node
    -   In the value portion of a yaml template key-value pair,
        -   everything between two escape sequences `/%   /%` is dynamically interpreted (see syntax, below).
        -   everything outside of `/%  /%` is treated as literal string values.
    -   You can have any number of escaped regions within a single value, and they may be interspersed with static text.
        -   Within each `/%   %/` escape sequence pair, you can include special directives:
            -   one or more diceroll specifications e.g. `1D6`, which will automatically be resolved to an actual number using a random number generator
            -   one or more table lookups
            -   one or more references to other nodes in the template. Such references are materialized on-demand.
            -   an arithmetic expression e.g. 4 \* 27

1.  Diceroll Specification Directive

    -   A diceroll specification is of the form ***`number of dice`* `D` *`number of sides`***
    -   Note that there are no spaces between the numbers and the `D`
    -   Also, the `D` may be upper- or lower-case ( `d` )
    -   Any number of sides is allowed - since these are virtual dice, you could specify a 13-sided die&#x2026;or even a 2,521-sided die(!)
    -   For example, here, we roll 3 6-sided dice to get the `STR` attribute value:
        
            STR: \% 3d6 \%

2.  Table Lookup Directive

    1.  Lookup Tables
    
        -   Like templates, tables are defined in plain text .yaml format
        -   Tables may be one- or two-dimensional (using either just a Y-index lookup, or both a Y- and an X- index lookup)
        -   Tables may use ranges of values; this results in slower lookups, but can save a lot of work in typing the actual table file
    
    2.  One-Dimensional Lookup Tables
    
        -   A one-dimensional table is a table that is referenced by a single value, e.g. you would use Age to look up the IQ value from this table:
            
            <table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">
            
            
            <colgroup>
            <col  class="org-right" />
            
            <col  class="org-right" />
            </colgroup>
            <thead>
            <tr>
            <th scope="col" class="org-right">Age</th>
            <th scope="col" class="org-right">IQ</th>
            </tr>
            </thead>
            
            <tbody>
            <tr>
            <td class="org-right">16</td>
            <td class="org-right">108</td>
            </tr>
            
            
            <tr>
            <td class="org-right">17</td>
            <td class="org-right">108</td>
            </tr>
            
            
            <tr>
            <td class="org-right">18</td>
            <td class="org-right">105</td>
            </tr>
            
            
            <tr>
            <td class="org-right">19</td>
            <td class="org-right">105</td>
            </tr>
            
            
            <tr>
            <td class="org-right">20</td>
            <td class="org-right">99</td>
            </tr>
            
            
            <tr>
            <td class="org-right">21</td>
            <td class="org-right">99</td>
            </tr>
            
            
            <tr>
            <td class="org-right">22</td>
            <td class="org-right">99</td>
            </tr>
            
            
            <tr>
            <td class="org-right">25</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">26</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">27</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">28</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">29</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">30</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">31</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">32</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">33</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">34</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">35</td>
            <td class="org-right">101</td>
            </tr>
            
            
            <tr>
            <td class="org-right">36</td>
            <td class="org-right">101</td>
            </tr>
            </tbody>
            </table>
        -   If we wanted to call this table "IQbyAge", we would implement the table in YAML like this:
            
                table:
                  name: IQbyAge
                  y-axis: number
                  entries: 
                    16:  108
                    17:  108
                    18:  105
                    19:  105
                    20:   99
                    21:   99
                    22:   99
                    25:   97
                    26:   97
                    27:   97
                    28:   97
                    29:   97
                    30:   97
                    31:   97
                    32:   97
                    33:   97
                    34:   97
                    35:  101
                    36:  101
        -   In a template, a one-dimensional table lookup is expressed thusly: **`[` *the\_table\_name* `|` *the lookup key* `]`**
        -   So, for example, if we were creating a template that uses the IQbyAge table (using a random Age lookup value), our lookup would like this:
            
                IQ: \% [IQbyAge|2D10+14]  \%
    
    3.  Two-Dimensional Lookup Tables
    
        -   A two-dimensional table is a table that is referenced by two values.
        -   e.g. You would use Age and Sex to look up the IQ value from this table:
            
            <table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">
            
            
            <colgroup>
            <col  class="org-right" />
            
            <col  class="org-right" />
            
            <col  class="org-right" />
            </colgroup>
            <thead>
            <tr>
            <th scope="col" class="org-right">Age</th>
            <th scope="col" class="org-right">Male</th>
            <th scope="col" class="org-right">Female</th>
            </tr>
            </thead>
            
            <tbody>
            <tr>
            <td class="org-right">16</td>
            <td class="org-right">108</td>
            <td class="org-right">109</td>
            </tr>
            
            
            <tr>
            <td class="org-right">17</td>
            <td class="org-right">108</td>
            <td class="org-right">109</td>
            </tr>
            
            
            <tr>
            <td class="org-right">18</td>
            <td class="org-right">105</td>
            <td class="org-right">106</td>
            </tr>
            
            
            <tr>
            <td class="org-right">19</td>
            <td class="org-right">105</td>
            <td class="org-right">106</td>
            </tr>
            
            
            <tr>
            <td class="org-right">20</td>
            <td class="org-right">99</td>
            <td class="org-right">100</td>
            </tr>
            
            
            <tr>
            <td class="org-right">21</td>
            <td class="org-right">99</td>
            <td class="org-right">100</td>
            </tr>
            
            
            <tr>
            <td class="org-right">22</td>
            <td class="org-right">99</td>
            <td class="org-right">99</td>
            </tr>
            
            
            <tr>
            <td class="org-right">23</td>
            <td class="org-right">98</td>
            <td class="org-right">98</td>
            </tr>
            
            
            <tr>
            <td class="org-right">24</td>
            <td class="org-right">98</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">25</td>
            <td class="org-right">97</td>
            <td class="org-right">96</td>
            </tr>
            
            
            <tr>
            <td class="org-right">26</td>
            <td class="org-right">97</td>
            <td class="org-right">95</td>
            </tr>
            
            
            <tr>
            <td class="org-right">27</td>
            <td class="org-right">97</td>
            <td class="org-right">94</td>
            </tr>
            
            
            <tr>
            <td class="org-right">28</td>
            <td class="org-right">97</td>
            <td class="org-right">95</td>
            </tr>
            
            
            <tr>
            <td class="org-right">29</td>
            <td class="org-right">97</td>
            <td class="org-right">96</td>
            </tr>
            
            
            <tr>
            <td class="org-right">30</td>
            <td class="org-right">97</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-right">31</td>
            <td class="org-right">97</td>
            <td class="org-right">98</td>
            </tr>
            
            
            <tr>
            <td class="org-right">32</td>
            <td class="org-right">97</td>
            <td class="org-right">99</td>
            </tr>
            
            
            <tr>
            <td class="org-right">33</td>
            <td class="org-right">97</td>
            <td class="org-right">100</td>
            </tr>
            
            
            <tr>
            <td class="org-right">34</td>
            <td class="org-right">97</td>
            <td class="org-right">101</td>
            </tr>
            
            
            <tr>
            <td class="org-right">35</td>
            <td class="org-right">101</td>
            <td class="org-right">102</td>
            </tr>
            
            
            <tr>
            <td class="org-right">36</td>
            <td class="org-right">101</td>
            <td class="org-right">103</td>
            </tr>
            </tbody>
            </table>
            
            -   this two-dimensional table would be implemented in yaml like this:
                
                    table:
                      name: IQbyAgeAndSex
                      y-axis: number
                      x-axis: sex
                      entries: 
                        16:  
                          Male: 108
                          Female: 109
                        17:    
                          Male: 108
                          Female: 109
                        18:    
                          Male: 105
                          Female: 106
                        19:    
                          Male: 105
                          Female: 106
                        20:    
                          Male: 99
                          Female: 100
                        21:    
                          Male: 99
                          Female: 100
                        22:    
                          Male: 99
                          Female: 99
                        25:    
                          Male: 97
                          Female: 96
                        26:    
                          Male: 108
                          Female: 95
                        27:    
                          Male: 108
                          Female: 94
                        28:    
                          Male: 108
                          Female: 95
                        29:    
                          Male: 108
                          Female: 96
                        30:    
                          Male: 108
                          Female: 97
                        31:    
                          Male: 108
                          Female: 98
                        32:    
                          Male: 108
                          Female: 99
                        33:    
                          Male: 108
                          Female: 100
                        34:    
                          Male: 108
                          Female: 101
                        35:    
                          Male: 108
                          Female: 102
                        36:    
                          Male: 108
                          Female: 103
            -   two-dimensional table lookups are denoted thusly: **`[` *thetable* `|` *lookup key Y* `|` *lookup key X* `]`**
            -   So, for example, the template entry for looking up IQ from the IQbyAgeAndSex table might look like this:
                
                    IQ: \% [IQbyAge|2D10+14|Male]  \%
    
    4.  Value Ranges
    
        -   It can be convenient to use value ranges in tables - it's less work to create and maintain a table that uses value ranges than the equivalent table that relies on discrete values.
            
            <table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">
            
            
            <colgroup>
            <col  class="org-left" />
            
            <col  class="org-right" />
            </colgroup>
            <thead>
            <tr>
            <th scope="col" class="org-left">Age Range</th>
            <th scope="col" class="org-right">IQ</th>
            </tr>
            </thead>
            
            <tbody>
            <tr>
            <td class="org-left">16–17</td>
            <td class="org-right">108</td>
            </tr>
            
            
            <tr>
            <td class="org-left">18–19</td>
            <td class="org-right">105</td>
            </tr>
            
            
            <tr>
            <td class="org-left">20–24</td>
            <td class="org-right">99</td>
            </tr>
            
            
            <tr>
            <td class="org-left">25–34</td>
            <td class="org-right">97</td>
            </tr>
            
            
            <tr>
            <td class="org-left">35–44</td>
            <td class="org-right">101</td>
            </tr>
            
            
            <tr>
            <td class="org-left">45–54</td>
            <td class="org-right">106</td>
            </tr>
            
            
            <tr>
            <td class="org-left">55–64</td>
            <td class="org-right">109</td>
            </tr>
            
            
            <tr>
            <td class="org-left">65–69</td>
            <td class="org-right">114</td>
            </tr>
            </tbody>
            </table>
            
            -   The above table would be implemented in a .yaml file like this:
                
                    table:
                      name: IQbyAge
                      y-axis: number
                      entries: 
                        "[16-17"]:  108
                        "[18-19"]:  105
                        "[20-24]":   99
                        "[25-34]":   97
                        "[35-44]":  101
                        "[45-54]":  106
                        "[55-64]":  109
                        "[65-69]":  114
        -   Note that the template's lookup expression would not change from the prior example:
            
                IQ: \% [IQbyAge|2D10+14]  \%
        -   You still pass a single numeric value to the table.

3.  Template Reference Directive

    -   Using a template reference directive, you can incorporate the value of another node in the template
    -   A reference is denoted using curly braces, and is expressed like a directory path e.g. `{template/name}`.
    -   Let's look at this simple example template that uses a template reference directive:
        
            template:
              template_name: Fighter
              Description: a generic fighter
              stats:
                ST: \% 3d6 \%
                DX: \% 2d6+6 \%
                IQ: \% 3d6 \%
                HP: \% {template/stats/ST}  \%
        
        -   In the above example, the `HP` stat is set to the value of the `ST` stat using a template reference directive.

4.  Combining Different Directives Within A Single Value

    -   Directives may be combined in limited ways; for example:
        -   references to other values within the template may be combined as part of an arithmetic expression.
            -   Here, we add the `STR` and `DEX` stats to derive the `Athletics` skill rank:
                
                    Athletics: \%{template/stats/STR} + {template/stats/DEX} \%
            -   Here, we combine the `INT` and `DEX` stats, and perform some additional math to derive action points:
                
                    action_points      : \% ((( {template/stats/INT} + {template/stats/DEX} ) -1 ) / 12 ) + 1  \%
        -   a table lookup can use a dice roll specification, as in this example:
            
                hair_color: \% [Hair Color|1D100] \%
        -   a table lookup can use another value in the template
            -   Here, we use the `SIZ` stat to look up the height from the `Human Height` table:
                
                    height: \% [Human Height|{template/stats/SIZ}] \%
        -   Here, we use both a template value reference directive and a random number directive within a table lookup directive to look up a name:
            
                name: \% [Names|{template/sex}|1d4391] \%
    -   A dice roll spec can be combined with an arithmetic expression and/or another value in the template
    -   Some combinations are not supported.
    -   able lookups cannot be nested
        -   as a workaround, you can capture an intermediate value in a template node, and then refer to that template node in another node definition&#x2026;
            -   Here's a simple example of using an intermediate value for a table lookup:
                
                    submarine_color: \% [Colors|1D12] \%
                    submarine_size: \%  [Size|{template/submarine_color}|1D6] \%
            -
    -   Another interesting thing you can do is to use literal yaml in your lookup tables (see the Medieval Weapons lookup table definition for an example of this technique)


### Template And Table Examples

-   The templates and tables included in this repository are mostly intended for NPC/monster generation, and are based on the rules for the Mythras tabletop role-playing game.
-   This tool is open-ended, however, so it should be possible to create tables and templates for most role-playing games.


# Dependencies

-   Scala 2.12 runtime
-   SnakeYAML


# Author

-   Jonathan Donald


# Acknowledgments

-   Mersenne Twister random number generation code is by jesperdj <https://gist.github.com/jesperdj/887771>
-   The Entity Generator relies on Andrey Somov's SnakeYAML Java library to parse YAML files
-   Inspiration came from various roleplaying games, especially: Steve Jackson's The Fantasy Trip, N. Robin Crossby's HarnMaster, Iron Crown Enterprises RoleMaster, and The Design Mechanism's Mythras (formerly RuneQuest 6)
-   Additional inspiration and ideas came from Hannu Kokko's wonderful RuneQuest 6/Mythras Encounter Generator


# License

-   This project is licensed under the GNU Lesser General Public License v3.0 - see the LICENSE file for details.

