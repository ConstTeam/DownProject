using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Text;
using UnityEngine;
using UnityEngine.UI;
using MS;

/// <summary>
/// 通用工具类
/// 提供一些常用功能的接口
/// </summary>
public static class CommonHelper
{
    /// <summary>
    /// 设置一个Image是否变灰
    /// </summary>
    /// <param name="image"></param>
    /// <param name="isGray"></param>
    //public static void SetImageGray(Image image, bool isGray)
    //{
    //    if (null == image)
    //    {
    //        Debug.LogWarning("需要指定一个Image");
    //        return;
    //    }
    //    image.color = isGray ? GloablDefine.ColorGray : GloablDefine.ColorWhite;
    //}

    public static void SetImageGray(Image image, bool isGray)
    {
        if (null == image)
        {
            Debug.LogWarning("需要指定一个RawImage");
            return;
        }
        if (isGray)
        {
            Material grayMat = ResourceLoader.LoadAsset<Material>("Material/ShaderGray"); //填入路径
            image.material = grayMat;
        }
        else
        {
            image.material = null;
        }
    }

    /// <summary>
    /// RawImage置灰
    /// </summary>
    /// <param name="rawImage"></param>
    /// <param name="isGray"></param>
    public static void SetRawImageGray(RawImage rawImage, bool isGray)
    {
        if (null == rawImage)
        {
            Debug.LogWarning("需要指定一个RawImage");
            return;
        }
        if (isGray)
        {
            Material grayMat = ResourceLoader.LoadAsset<Material>(""); //填入路径
            rawImage.material = grayMat;
            rawImage.color = Color.black;
        }
        else
        {
            rawImage.material = null;
            rawImage.color = Color.white;
        }
    }
    /// <summary>
    /// 获取当前运行的设备平台信息
    /// </summary>
    /// <returns></returns>
    public static string GetDeviceInfo()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.Append("Device And Sysinfo:\r\n");
        stringBuilder.Append(string.Format("DeviceModel: {0} \r\n", SystemInfo.deviceModel));
        stringBuilder.Append(string.Format("DeviceName: {0} \r\n", SystemInfo.deviceName));
        stringBuilder.Append(string.Format("DeviceType: {0} \r\n", SystemInfo.deviceType));
        stringBuilder.Append(string.Format("BatteryLevel: {0} \r\n", SystemInfo.batteryLevel));
        stringBuilder.Append(string.Format("DeviceUniqueIdentifier: {0} \r\n", SystemInfo.deviceUniqueIdentifier));
        stringBuilder.Append(string.Format("SystemMemorySize: {0} \r\n", SystemInfo.systemMemorySize));
        stringBuilder.Append(string.Format("OperatingSystem: {0} \r\n", SystemInfo.operatingSystem));
        stringBuilder.Append(string.Format("GraphicsDeviceID: {0} \r\n", SystemInfo.graphicsDeviceID));
        stringBuilder.Append(string.Format("GraphicsDeviceName: {0} \r\n", SystemInfo.graphicsDeviceName));
        stringBuilder.Append(string.Format("GraphicsDeviceType: {0} \r\n", SystemInfo.graphicsDeviceType));
        stringBuilder.Append(string.Format("GraphicsDeviceVendor: {0} \r\n", SystemInfo.graphicsDeviceVendor));
        stringBuilder.Append(string.Format("GraphicsDeviceVendorID: {0} \r\n", SystemInfo.graphicsDeviceVendorID));
        stringBuilder.Append(string.Format("GraphicsDeviceVersion: {0} \r\n", SystemInfo.graphicsDeviceVersion));
        stringBuilder.Append(string.Format("GraphicsMemorySize: {0} \r\n", SystemInfo.graphicsMemorySize));
        stringBuilder.Append(string.Format("GraphicsMultiThreaded: {0} \r\n", SystemInfo.graphicsMultiThreaded));
        stringBuilder.Append(string.Format("SupportedRenderTargetCount: {0} \r\n", SystemInfo.supportedRenderTargetCount));
        return stringBuilder.ToString();
    }

    /// <summary>
    /// 获取设备的电量
    /// </summary>
    /// <returns></returns>
    public static float GetBatteryLevel()
    {
        if (Application.isMobilePlatform)
        {
            return SystemInfo.batteryLevel;
        }

        return 1;
    }

    /// <summary>
    /// 获取设备的电池状态
    /// </summary>
    /// <returns></returns>
    public static BatteryStatus GetBatteryStatus()
    {
        return SystemInfo.batteryStatus;
    }

    /// <summary>
    /// 获取设备网络的状况
    /// </summary>
    /// <returns></returns>
    public static NetworkReachability GetNetworkStatus()
    {
        return Application.internetReachability;
    }

    
    /// <summary>
    /// 将世界坐标转化UGUI坐标
    /// </summary>
    /// <param name="gameCamera"></param>
    /// <param name="canvas"></param>
    /// <param name="worldPos"></param>
    /// <returns></returns>
    public static Vector2 WorldToUIPoint(Camera gameCamera, Canvas canvas, Vector3 worldPos)
    {
        Vector2 pos;
        RectTransformUtility.ScreenPointToLocalPointInRectangle(canvas.transform as RectTransform,
            gameCamera.WorldToScreenPoint(worldPos), canvas.worldCamera, out pos);
        return pos;
    }

  
    /// <summary>
    /// 判断一个string数组中是否包含某个string
    /// </summary>
    /// <param name="key"></param>
    /// <param name="strList"></param>
    /// <returns></returns>
    public static bool IsArrayContainString(string key, params string[] strList)
    {
        if (null == key || null == strList)
        {
            return false;
        }
        for (int i = 0; i < strList.Length; i++)
        {
            if (strList[i].Equals(key))
            {
                return true;
            }
        }
        return false;
    }

    /// <summary>
    /// 获取主机IP地址
    /// </summary>
    /// <returns></returns>
    public static string GetHostIp()
    {
        String url = "http://hijoyusers.joymeng.com:8100/test/getNameByOtherIp";
        string IP = "未获取到外网ip";
        try
        {
            System.Net.WebClient client = new System.Net.WebClient();
            client.Encoding = Encoding.Default;
            string str = client.DownloadString(url);
            client.Dispose();

            if (!str.Equals("")) IP = str;
        }
        catch (Exception e)
        {
            Debug.LogWarning(e.ToString());
        }
        Debug.Log("get host ip :" + IP);
        return IP;
    }

    /// <summary>
    /// 获取一个GameObject下所有子物体的数量
    /// </summary>
    /// <param name="obj"></param>
    /// <returns></returns>
    public static int ChildCount(this GameObject obj)
    {
        if (null != obj)
        {
            return obj.transform.childCount;
        }

        return 0;
    }

    /// <summary>
    /// 根据索引获取一个GameOject的子物体
    /// </summary>
    /// <param name="obj"></param>
    /// <param name="index"></param>
    /// <returns></returns>
    public static GameObject GetChild(this GameObject obj, int index)
    {
        if (null != obj)
        {
            return obj.transform.GetChild(index).gameObject;
        }

        return null;
    }


    public static void Set3DTextColor(TextMesh tm,Color32 color)
    {
        tm.GetComponent<MeshRenderer>().material.color = color;      
    }

    public static void ResetLayer(GameObject obj,int layer)
    {
        foreach (Transform child in obj.GetComponentsInChildren<Transform>())
        {
            child.gameObject.layer = layer;
        }
    }

    public static void SetTextAlpha(Text txt, float alpha)
    {
        Color color = new Color(txt.color.r, txt.color.g, txt.color.b, alpha);
        txt.color = color;
    }


    public static void SetImageAlpha(Image img,float alpha)
    {
        Color color = new Color(img.color.r, img.color.g, img.color.b, alpha);
        img.color = color;
    }
}
