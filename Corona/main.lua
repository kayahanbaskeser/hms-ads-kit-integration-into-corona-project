-- Author Kayahan Baskeser 
-- https://github.com/kayahanbaskeser

local ads = require "plugin.huaweiads"
local json = require("json")
local widget = require( "widget" )


local function listener( event )
	print( "Received event from huaweiads plugin " , json.prettify(event))
end

ads.init( listener )
ads.showBanner()

local function onButtonClick( event )
	ads.InterstitialAd()
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
