# AnvilLore
## A lightweight plugin for adding item lore's in anvil

### Contributing

1. Clone repository using `git clone https://github.com/INotSleep/AnvilLore.git`
2. Make your changes
3. Build using `gradlew build`
4. Use `./build/libs/AnvilLore-*-all.jar` file as plugin
5. Make pull request
6. Done :)

### Configuration

```yml
settings:                                            # General settings
  allowInCombine: true                               # Allow adding lore while combine 2 items
  ignoreCase: true                                   # Ignore case in prefix 
  addLorePrefix: 'lore:'                             # Prefix to add lore
  removeFormattingWhileNoPerms: true                 # Remove color codes and etc when player not having perms to set colors
  allowMultiLine: true                               # Allow multiple lines in lore
  messageNoFunds: true                               # Display "notEnoughMoney" message when not enought money to add lore
price:                                               # Price setting
  type: ECONOMY                                      # Type of price. Allowed ECONOMY, LEVELS, POINTS
  baseValue: 100.0                                   # Base price
  expression: base+(symbols*1.3)+base*lines          # Expression of price. Base - base price. Symbols - Symbols in this line. Lines - count of custom lore lines
messages:                                            # Messages settings
  notEnoughMoney: '&4Not enought money. Need: {0}$'  
  price: '&aPrice: {0}$'                             
permissions:                                         # Permissions
  use: anvillore.use                                 # Permission, that allows add permission
  limit:                                             # Limit permissions
    bypass: anvillore.limit.bypass                   # Permission to Bypass limit (When allowMultiLine option is enabled)
    prefix: anvillore.limit.                         # Prefix for limit permission. Ex. anvillore.limit.3 (3 Lines)
  formatting: anvillore.formatting                   # Permission to allow color codes and etc.

```

###
