package com.trackingdeluxe.speedometer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.trackingdeluxe.speedometer.R
import com.trackingdeluxe.speedometer.data.enums.MetricType
import com.trackingdeluxe.speedometer.ui.base.BaseActivity

/**abstract adapter Base adapter to other adapters
 * @param T generic type to child adapters
 */
abstract class AppBaseAdapter<T>(protected val context: BaseActivity) :
    RecyclerView.Adapter<AppBaseAdapter.BaseItem<T>>() {
    var items: MutableList<T> = mutableListOf()
    lateinit var metricType: MetricType

    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, i: Int): BaseItem<T> {
        return createHolder(LayoutInflater.from(context).inflate(getLayoutId(), viewGroup, false))
    }

    override fun getItemCount() = items.size

    /**provide layout id of item in child adapter**/
    abstract fun getLayoutId(): Int

    /**get String from resource
     * @param id - resource String id
     */
    fun getString(id: Int) =
        context.getString(id)

    /**add item to adapter
     * @param item to adapter and update recycler view
     */
    open fun addItem(item: T) {
        items.add(item)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BaseItem<T>, position: Int) {
        items[position]?.let { holder.bind(it) }
    }

    /**create baseItem in child adapters
     *@param inflate - inflated view
     */
    abstract fun createHolder(inflate: View): BaseItem<T>

    /**remove all item from adapter and update recycler view*/
    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    /**remove removed from db in adapter
     * update recycler view
     *@param item removed item from db
     *@param baseItem - base holder to provide remove animation
     **/
    fun removeItemByPosition(item: T, baseItem: BaseItem<T>) {
        if (!isRemoveItemAnimationEnable()) {
            items.remove(item)
            notifyDataSetChanged()
            return
        }
        baseItem.removeItemAnimation(context, object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                baseItem.itemView.visibility = View.GONE
                items.remove(item)
                notifyDataSetChanged()
            }

            override fun onAnimationStart(animation: Animation) {

            }
        })
    }

    /**
     *flag to say need remove with animation or not
     *can be overriding in child adapters
     **/
    open fun isRemoveItemAnimationEnable() = true

    /**add items to adapter
     *@param items list of items to show in recycler view
     **/
    fun addItems(items: List<T>) {
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    /**base holder to holders in children adapters
     * @param itemView - inflating holder view
     **/
    abstract class BaseItem<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * bind item to item view
         * @param t item from items to bind in adapter
         */
        abstract fun bind(t: T)

        /**
         * create removing animation
         * @param context - context to load animation
         * @param listener - animationListener to inject in animation
         */
        open fun removeItemAnimation(
            context: AppCompatActivity,
            listener: Animation.AnimationListener
        ) {
            getRemoveItemAnimation(context)?.let { animation ->
                animation.setAnimationListener(listener)
                itemView.startAnimation(animation)
            }
        }

        /**
         * create removing animation
         * @param context - context to load animation
         */
        private fun getRemoveItemAnimation(context: Context): Animation? =
            AnimationUtils.loadAnimation(context, R.anim.base_remove_item_animation)
                .apply {
                    isFillEnabled = false
                    duration = 400
                }
    }
}