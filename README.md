# Exchange rate calculator


## Requirements
- Build an exchange rate calculator that converts between USDc and other currencies
- The application should provide a good user experience
- Consider edge cases and how users will interact with the app
- Feel free to use system default font instead of the custom specified in the designs.

### Core Feature Requirements

1. Currency Input Fields
    - Two input fields: one for USDc and one for the selected currency
    - Users should be able to enter amounts in either field
    - The app should automatically calculate and update the corresponding amount in the other field
   
2. Currency Selection
    - Tapping the non-USDc currency should open a bottom sheet where users can select a different currency
   
3. Currency Swap
    - Include a swap button (arrow) between the two currency fields
    - Tapping this button should swap the positions of the two currencies
   
4. Working Application
    - The app must be fully functional and demonstrate all the features listed above

### Additional Features

I added the following extra features that made sense to me:

1. Currency Input Fields
   - The user can select-all, copy, cut, and paste into the text fields
   - The text fields are automatically formatted to the appropriate currency
   - The conversion is done at full precision, but the currencies are displayed with the correct decimal places for that currency

2. Currency selection
   - The domestic (USDC) currency value stays constant when choosing between other currencies
   - The conversion for the newly selected currency is done automatically

3. Currency Swap
    - Tapping the currency swap button swaps between "bid" and "ask" modes
    - The most recently edited currency value stays the same, and the other one is recalculated with the new rate

4. Local Caching
   - Exchange rates are cached locally
   - If the cached value is over 2 hours old when selected, it's automatically loaded from the network
   - There's a button on the UI to force-refresh the exchange rate

5. Demo-proof Networking 
   - At time of writing, the "https://api.dolarapp.dev/v1/tickers-currencies" API is still returning a 403
   - If the network returns a 403 for this path, the app will fall back to a "fake" implementation
   - The fake implementation will always list the same four currencies = [MXN, ARS, BRL, COP]

### Next Steps
- Unit test coverage! I wrote some as a demonstration of how I like to write tests, but more coverage is needed
- Local cache for country list
- The "Loading" and "Error" UI states are a bit rough

## Logic Notes
### Conversion Flow
As the user types into the text field, the following process happens:

1. The text gets parsed to a number, but kept as a string to keep precision. 
   - e.g. USD$1,000.23 becomes 1000.23
2. The current text field is updated
   - The parsed number is formatted with the correct currency
   - The text-selection is maintained (either all, or last)
3. The other text field is computed using the parsed number
   - The correct conversion rate is determined
      - If the USDc value is at the top, then we're buying foreign and so we use the BID rate
      - If the foreign value is at the top, then we're buying USDc and so we use the ASK rate
      - If the user is typing into the foreign field, the rate is inverted
   - The [CurrencyExchanger] class does the conversion
     - The parsed number and conversion rates are converted to [BigDecimal]
     - Precision is arbitrarily set to 500. It can be any number, but even 500 chars is a very large amount of money!
     - The result is converted back to a plain string to keep precision
   - The converted string is formatted with the correct currency
   - The text-selection is maintained, in practice it's always set to the end of the input

### Swap Flow
When the user presses the swap button, the following happens:

1. The "mode" is switched between BID and ASK
2. The most-recently-edited text field is refreshed
3. The refresh causes the other text field to be recalculated as described above


