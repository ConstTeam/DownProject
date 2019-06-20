cd ./bin
rd /s /q .temp
Excel2Txt.exe ../../../Config ./.temp

PackConfig.exe ./.temp ./.config Default Chinese_Simplified
copy /y .\.config\Default\cfg ..\..\..\..\project\client\Assets\StreamingAssets\UpdatePkg\cfg.ms
pause