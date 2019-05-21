using System;
using UnityEngine;
using System.Collections;
using UnityEngine.Events;
using UnityEngine.EventSystems;

public class UIEventListenerDrag : MonoBehaviour, IDragHandler, IPointerDownHandler, IPointerEnterHandler, IPointerExitHandler, IPointerUpHandler
{
    public delegate void VoidDelegate(GameObject go);

    public VoidDelegate onDrag;
    public VoidDelegate onDown;
    public VoidDelegate onEnter;
    public VoidDelegate onExit;
    public VoidDelegate onUp;

    static public UIEventListenerDrag Get(GameObject go)
    {
        UIEventListenerDrag listener = go.GetComponent<UIEventListenerDrag>();
        if (listener == null) listener = go.AddComponent<UIEventListenerDrag>();
        return listener;
    }

    public void OnPointerDown(PointerEventData eventData)
    {
        if (onDown != null) onDown(gameObject);
    }
    public void OnPointerEnter(PointerEventData eventData)
    {
        if (onEnter != null) onEnter(gameObject);
    }
    public void OnPointerExit(PointerEventData eventData)
    {
        if (onExit != null) onExit(gameObject);
    }
    public void OnPointerUp(PointerEventData eventData)
    {
        if (onUp != null) onUp(gameObject);
    }
    public void OnDrag(PointerEventData eventData)
    {
        if (onDrag != null) onDrag(gameObject);
    }

}
