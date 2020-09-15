-- Author Kayahan Baskeser 
-- https://github.com/kayahanbaskeser

local library = require "plugin.library"
local json = require("json")
local widget = require( "widget" )


local function listener( event )
	print( "Received event from Library plugin " , json.prettify(event))
end

library.init( listener )
library.showBanner()

local function onButtonClick( event )
	library.InterstitialAd()
end

-- Create the widget
local button = widget.newButton(
    {
        left = 75,
        top = 200,
        id = "InterstitialAd",
        label = "InterstitialAd",
        onEvent = onButtonClick
    }
)
