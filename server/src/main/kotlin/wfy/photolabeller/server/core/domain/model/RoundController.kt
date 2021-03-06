package wfy.photolabeller.server.core.domain.model

interface RoundController {
    fun startRound(): UpdatingRound
    fun freezeRound()
    fun endRound(): Boolean
    fun checkCurrentRound(): Boolean
    fun onNewClientUpdate()
    fun getCurrentRound(): UpdatingRound
    fun currentRoundToJson(): String
}