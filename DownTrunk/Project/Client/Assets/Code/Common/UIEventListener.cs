using System;
using UnityEngine;
using System.Collections;
using UnityEngine.Events;
using UnityEngine.EventSystems;



public class UIEventListener :MonoBehaviour,IPointerClickHandler,IPointerDownHandler,IPointerEnterHandler,IPointerExitHandler,IPointerUpHandler
                              ,IMoveHandler,ICancelHandler,IDeselectHandler,ISelectHandler,ISubmitHandler,IUpdateSelectedHandler
{
    public delegate void VoidDelegate(GameObject go);
    public delegate void BaseDelegate(GameObject go, BaseEventData eventData);
    public delegate void AxisDelegate(GameObject go, AxisEventData eventData);
    public delegate void PointerDelegate(GameObject go, PointerEventData eventData);

    public object parameter;
    
    public VoidDelegate     onClick;
    public VoidDelegate     onDown;
    public VoidDelegate     onEnter;
    public VoidDelegate     onExit;
    public VoidDelegate     onUp;
    public AxisDelegate     onMove;
    public BaseDelegate     onCancel;
    public BaseDelegate     onDeselect;
    public BaseDelegate     onSelect;
    public BaseDelegate     onSubmit;
    public BaseDelegate     onUpdateSelected;
    static public UIEventListener Get(GameObject go)
    {
        UIEventListener listener = go.GetComponent<UIEventListener>();
        if (listener == null) listener = go.AddComponent<UIEventListener>();
        return listener;
    }

    public  void OnCancel(BaseEventData eventData)
    {
        if (onCancel != null) onCancel(gameObject, eventData);
    }
    public  void OnDeselect(BaseEventData eventData)
    {
        if (onDeselect != null) onDeselect(gameObject, eventData);
    }
    public  void OnMove(AxisEventData eventData)
    {
        if (onMove != null) onMove(gameObject, eventData);
    }
    public void OnPointerClick(PointerEventData eventData)
    {
        if (onClick != null) onClick(gameObject); 
    }
    public void OnPointerDown(PointerEventData eventData)
    {
        if (onDown != null) onDown(gameObject);
    }
    public  void OnPointerEnter(PointerEventData eventData)
    {
        if (onEnter != null) onEnter(gameObject);
    }
    public  void OnPointerExit(PointerEventData eventData)
    {
        if (onExit != null) onExit(gameObject);
    }
    public  void OnPointerUp(PointerEventData eventData)
    {
        if (onUp != null) onUp(gameObject);
    }
    public void OnSelect(BaseEventData eventData)
    {
        if (onSelect != null) onSelect(gameObject, eventData);
    }
    public void OnSubmit(BaseEventData eventData)
    {
        if (onSubmit != null) onSubmit(gameObject,eventData);
    }
    public void OnUpdateSelected(BaseEventData eventData)
    {
        if (onUpdateSelected != null) onUpdateSelected(gameObject, eventData);
    }
}
